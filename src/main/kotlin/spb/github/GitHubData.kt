package spb.github

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class GitHubData(val html_url : String, val url : String)