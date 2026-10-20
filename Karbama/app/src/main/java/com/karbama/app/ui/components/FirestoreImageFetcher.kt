package com.karbama.app.ui.components

import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import com.karbama.app.data.repo.FirestoreFiles
import okio.Buffer

/**
 * v48: Coil آدرس‌های fsimg://… را از Firestore می‌خواند
 * (عکس‌های متقاضی و آگهی که داخل Firestore ذخیره شده‌اند).
 */
class FirestoreImageFetcher(
    private val address: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val bytes = FirestoreFiles.read(address)
        return SourceResult(
            source = ImageSource(Buffer().write(bytes), options.context),
            mimeType = "image/jpeg",
            dataSource = DataSource.NETWORK
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? =
            if (data.scheme == FirestoreFiles.SCHEME) FirestoreImageFetcher(data.toString(), options)
            else null
    }
}
