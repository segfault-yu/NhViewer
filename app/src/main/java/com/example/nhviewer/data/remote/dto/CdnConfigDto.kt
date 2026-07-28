package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.CdnConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 接口返回一组内容一致的镜像服务器，而非单一主机
@Serializable
data class CdnConfigDto(
    @SerialName("image_servers") val imageServers: List<String> = emptyList(),
    @SerialName("thumb_servers") val thumbServers: List<String> = emptyList()
)

// 取列表首个作为主用主机；列表为空时整体回退到默认镜像
fun CdnConfigDto.toDomain(): CdnConfig {
    val images = imageServers.filter { it.isNotBlank() }.ifEmpty { CdnConfig.DEFAULT.imageHosts }
    val thumbs = thumbServers.filter { it.isNotBlank() }.ifEmpty { CdnConfig.DEFAULT.thumbHosts }
    return CdnConfig(
        primaryImageHost = images.first(),
        primaryThumbHost = thumbs.first(),
        imageHosts = images,
        thumbHosts = thumbs
    )
}
