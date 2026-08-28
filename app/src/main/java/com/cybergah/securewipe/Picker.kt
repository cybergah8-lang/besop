package com.cybergah.securewipe

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.documentfile.provider.DocumentFile

/**
 * ACTION_OPEN_DOCUMENT'in hazir sozlesmesi sadece OKUMA izni ister.
 * Uzerine yazabilmek icin YAZMA + kalici izin bayraklarini da eklememiz gerekiyor.
 */
class OpenDocumentsRw : ActivityResultContract<Unit, List<Uri>>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (resultCode != Activity.RESULT_OK || intent == null) return emptyList()
        val out = ArrayList<Uri>()
        intent.clipData?.let { cd ->
            for (i in 0 until cd.itemCount) out.add(cd.getItemAt(i).uri)
        }
        if (out.isEmpty()) intent.data?.let { out.add(it) }
        return out
    }
}

/** Klasor secimi (yazma izniyle). */
class OpenTreeRw : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == Activity.RESULT_OK) intent?.data else null
}

/** Bir klasor agacini duz dosya listesine acar. */
fun expandTree(context: Context, treeUri: Uri): List<Uri> {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
    val out = ArrayList<Uri>()
    fun walk(dir: DocumentFile, depth: Int) {
        if (depth > 12) return
        for (f in dir.listFiles()) {
            if (f.isDirectory) walk(f, depth + 1) else if (f.isFile) out.add(f.uri)
        }
    }
    walk(root, 0)
    return out
}
