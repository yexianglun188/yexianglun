package com.campus.market.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /** 上传图片，返回可访问的相对地址 */
    String uploadImage(MultipartFile file);
}
