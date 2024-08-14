package schemact.gradleplugin.aws.util

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.transfer.*
import java.io.File
import java.util.*

//based on https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/java/example_code/s3/src/main/java/aws/example/s3/XferMgrProgress.java
class XferMgrProgress {
    // waits for the transfer to complete, catching any exceptions that occur.

    companion object {
        fun waitForCompletion(xfer: Transfer) {
            // snippet-start:[s3.java1.s3_xfer_mgr_progress.wait_for_transfer]
            try {
                xfer.waitForCompletion()
            } catch (e: AmazonServiceException) {
                System.err.println("Amazon service error: " + e.message)
                System.exit(1)
            } catch (e: AmazonClientException) {
                System.err.println("Amazon client error: " + e.message)
                System.exit(1)
            } catch (e: InterruptedException) {
                System.err.println("Transfer interrupted: " + e.message)
                System.exit(1)
            }
            // snippet-end:[s3.java1.s3_xfer_mgr_progress.wait_for_transfer]
        }

        // Prints progress of a multiple file upload while waiting for it to finish.
        fun showMultiUploadProgress(multi_upload: MultipleFileUpload) {
            // print the upload's human-readable description
            println(multi_upload.description)

            // snippet-start:[s3.java1.s3_xfer_mgr_progress.substranferes]
            var sub_xfers: Collection<Upload> = ArrayList()
            sub_xfers = multi_upload.subTransfers
            do {
                println("\nSubtransfer progress:\n")
                for (u in sub_xfers) {
                    println("  " + u.description)
                    if (u.isDone) {
                        val xfer_state = u.state
                        println("  $xfer_state")
                    } else {
                        val progress = u.progress
                        val pct = progress.percentTransferred
                        printProgressBar(pct)
                        println()
                    }
                }

                // wait a bit before the next update.
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    return
                }
            } while (multi_upload.isDone == false)
            // print the final state of the transfer.
            val xfer_state = multi_upload.state
            println("\nMultipleFileUpload $xfer_state")
            // snippet-end:[s3.java1.s3_xfer_mgr_progress.substranferes]
        }

        // Prints progress while waiting for the transfer to finish.
        fun showTransferProgress(xfer: Transfer) {
            // snippet-start:[s3.java1.s3_xfer_mgr_progress.poll]
            // print the transfer's human-readable description
            println(xfer.description)
            // print an empty progress bar...
            printProgressBar(0.0)
            // update the progress bar while the xfer is ongoing.
            do {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    return
                }
                // Note: so_far and total aren't used, they're just for
                // documentation purposes.
                val progress = xfer.progress
                val so_far = progress.bytesTransferred
                val total = progress.totalBytesToTransfer
                val pct = progress.percentTransferred
                eraseProgressBar()
                printProgressBar(pct)
            } while (xfer.isDone == false)
            // print the final state of the transfer.
            val xfer_state = xfer.state
            println(": $xfer_state")
            // snippet-end:[s3.java1.s3_xfer_mgr_progress.poll]
        }


        // prints a simple text progressbar: [#####     ]
        fun printProgressBar(pct: Double) {
            // if bar_size changes, then change erase_bar (in eraseProgressBar) to
            // match.
            val bar_size = 40
            val empty_bar = "                                        "
            val filled_bar = "########################################"
            val amt_full = (bar_size * (pct / 100.0)).toInt()
            System.out.format(
                "  [%s%s]", filled_bar.substring(0, amt_full),
                empty_bar.substring(0, bar_size - amt_full)
            )
        }

        // erases the progress bar.
        fun eraseProgressBar() {
            // erase_bar is bar_size (from printProgressBar) + 4 chars.
            val erase_bar =
                "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b"
            System.out.format(erase_bar)
        }

        fun uploadFileWithListener(
            file_path: String,
            bucket_name: String?, key_prefix: String?, pause: Boolean
        ) {
            println(
                "file: " + file_path +
                        if (pause) " (pause)" else ""
            )
            var key_name: String? = null
            key_name = if (key_prefix != null) {
                "$key_prefix/$file_path"
            } else {
                file_path
            }

            // snippet-start:[s3.java1.s3_xfer_mgr_progress.progress_listener]
            val f = File(file_path)
            val xfer_mgr = TransferManagerBuilder.standard().build()
            try {
                val u = xfer_mgr.upload(bucket_name, key_name, f)
                // print an empty progress bar...
                printProgressBar(0.0)
                u.addProgressListener(object : ProgressListener {
                    override fun progressChanged(e: ProgressEvent) {
                        val pct: Double = e.getBytesTransferred() * 100.0 / e.getBytes()
                        eraseProgressBar()
                        printProgressBar(pct)
                    }
                })
                // block with Transfer.waitForCompletion()
                waitForCompletion(u)
                // print the final state of the transfer.
                val xfer_state = u.state
                println(": $xfer_state")
            } catch (e: AmazonServiceException) {
                System.err.println(e.errorMessage)
                System.exit(1)
            }
            xfer_mgr.shutdownNow()
            // snippet-end:[s3.java1.s3_xfer_mgr_progress.progress_listener]
        }

        fun uploadDirWithSubprogress(
            dir_path: String,
            bucket_name: String?, key_prefix: String?, recursive: Boolean,
            pause: Boolean
        ) {
            println("directory: " + dir_path + (if (recursive) " (recursive)" else "") + if (pause) " (pause)" else "")
            val xfer_mgr = TransferManager()
            try {
                val multi_upload = xfer_mgr.uploadDirectory(
                    bucket_name, key_prefix, File(dir_path), recursive
                )
                // loop with Transfer.isDone()
                showMultiUploadProgress(multi_upload)
                // or block with Transfer.waitForCompletion()
                waitForCompletion(multi_upload)
            } catch (e: AmazonServiceException) {
                System.err.println(e.errorMessage)
                System.exit(1)
            }
            xfer_mgr.shutdownNow()
        }

    }


}
