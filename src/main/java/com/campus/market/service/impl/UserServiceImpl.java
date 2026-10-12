package com.campus.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.PageResult;
import com.campus.market.common.ResultCode;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.LoginVO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.dto.UserUpdateDTO;
import com.campus.market.entity.User;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.UserService;
import com.campus.market.util.JwtUtil;
import com.campus.market.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException("该用户名已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setStudentNo(dto.getStudentNo());
        user.setCampus(dto.getCampus());
        user.setRole(User.ROLE_USER);
        user.setStatus(User.STATUS_NORMAL);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        // 用户不存在与密码错误返回同样提示，避免账号枚举
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == User.STATUS_DISABLED) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        String token = jwtUtil.createToken(user.getId(), user.getRole());
        return new LoginVO(token, user);
    }

    @Override
    public User getCurrentUser() {
        return getById(UserContext.requireUserId());
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserUpdateDTO dto) {
        Long userId = UserContext.requireUserId();
        User update = new User();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setPhone(dto.getPhone());
        update.setCampus(dto.getCampus());
        update.setAvatar(dto.getAvatar());
        userMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("新密码长度需为6-20位");
        }
        User user = getById(UserContext.requireUserId());
        if (!BCrypt.checkpw(oldPassword == null ? "" : oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(update);
    }

    @Override
    public PageResult<User> pageUsers(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StrUtil.isNotBlank(keyword), User::getUsername, keyword)
                .or(StrUtil.isNotBlank(keyword))
                .like(StrUtil.isNotBlank(keyword), User::getNickname, keyword)
                .orderByDesc(User::getCreateTime);
        userMapper.selectPage(page, wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user.getRole() == User.ROLE_ADMIN) {
            throw new BusinessException("不能操作管理员账号");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
    }
}
