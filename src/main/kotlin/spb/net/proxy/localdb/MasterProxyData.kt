package spb.net.proxy.localdb

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class MasterProxyData(var ip : String, var port : Int, var ports : IntArray?= null, var protocol : String? = null, var protocols : Array<String>, var detected : Boolean,
                      var type : String? = null, var risk : Int, var asn : String? = null, var provider : String? = null, var continent : String? = null,
                      var country : String? = null, var isocode : String? = null, var region : String? = null, var regioncode : String? = null,
                      var city : String? = null, var latitude : Double, var longitude : Double, var ipToLong : Long)