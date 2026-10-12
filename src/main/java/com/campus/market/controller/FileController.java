package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @RequireLogin
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success("上传成功", fileService.uploadImage(file));
    }
}
