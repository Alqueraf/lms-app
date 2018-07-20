package gc

import java.io.File
import java.util.regex.Pattern

data class ParsedCluster(
        val json: String,
        val projectId: String,
        val zone: String
)

object ClustersTxt {

    private val projZoneRegex = Pattern.compile(".*/projects/([^/]+)/zones/([^/]+)/clusters$")

    fun parse(path: String): ParsedCluster {
        val file = File(path)
        if (!file.exists()) throw RuntimeException("File doesn't exist! $file")
        // remove blank lines to ensure json drop logic works consistently
        val lines = file.readLines().filterNot { it.isBlank() }

        val post = lines.first()
        val matcher = projZoneRegex.matcher(post)
        if (!matcher.matches()) throw RuntimeException("Unable to match $projZoneRegex on $post")

        val projectId = matcher.group(1)
        val zone = matcher.group(2)

        // remove outer cluster JSON
        // POST ...       // drop
        // {              // drop
        //   "cluster": { // drop
        //   }
        // }  // drop
        val json = "{" + lines.drop(3).dropLast(1).joinToString(separator = "\n")

        return ParsedCluster(json = json, projectId = projectId, zone = zone)
    }
}
