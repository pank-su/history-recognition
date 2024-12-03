package su.pank

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("ListBucketResult", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", prefix = "ns")
data class ListBucketResult(
    @XmlElement(true) val Name: String,
    @XmlElement(true) val Prefix: String? = null,
    @XmlElement(true) val MaxKeys: Int,
    @XmlElement(true) val IsTruncated: Boolean,
    @XmlElement(true) val Contents: List<S3Object>? = null
)

@Serializable
@XmlSerialName("Contents", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
data class S3Object(
    @XmlElement(true) val Key: String,
    @XmlElement(true) val LastModified: String,
    @XmlElement(true) val Owner: Owner,
    @XmlElement(true) val ETag: String,
    @XmlElement(true) val Size: Long,
    @XmlElement(true) val StorageClass: String
)

@Serializable
data class Owner(
    @XmlElement(true) val ID: String? = null,
    @XmlElement(true) val DisplayName: String? = null
)