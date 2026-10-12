package com.campus.market.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.market.common.BusinessException;
import com.campus.market.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /** 允许的图片扩展名白名单 */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Value("${campus.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${campus.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片不能超过10MB");
        }

        String ext = FileUtil.extName(file.getOriginalFilename());
        if (StrUtil.isBlank(ext) || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException("只支持 jpg / png / gif / webp 格式的图片");
        }

        // 按日期分目录存放，避免单目录文件过多
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
        File dir = new File(uploadPath, datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败");
        }

        String fileName = IdUtil.fastSimpleUUID() + "." + ext.toLowerCase();
        File target = new File(dir, fileName);
        try {
            file.transferTo(target.getAbsoluteFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("图片上传失败，请重试");
        }
        return urlPrefix + datePath + "/" + fileName;
    }
}
