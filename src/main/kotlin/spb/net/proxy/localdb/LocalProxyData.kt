package spb.net.proxy.localdb

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class LocalProxyData(var id : Int, var ip : String, var port : Int, var asn : String? = null, var requester : String? = null,
                     var provider : String? = null, var continent : String? = null, var country : String? = null, var isocode : String? = null,
                     var region : String? = null, var regioncode : String? = null, var city : String? = null, var latitude : Double,
                     var longitude : Double, var proxy : Int, var type : String? = null, var risk : Int,
                     var status : String? = null, var timestamp : Long)