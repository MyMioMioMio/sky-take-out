package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端密码进行md5加密后在比对
        //spring自带的md5加密,数据库中存储的是大写
        password = DigestUtils.md5DigestAsHex(password.getBytes()).toUpperCase();
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //将数据复制到employee实体中
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置默认密码123456并且md5加密,大写字母
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()).toUpperCase());
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
//        //设置创建日期
//        employee.setCreateTime(LocalDateTime.now());
//        //设置最后修改日期
//        employee.setUpdateTime(LocalDateTime.now());
//        // 动态从threadlocal中获取创建人和修改人id
//        //设置创建人id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        //设置最后修改人id
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 调用mapper添加员工
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //页数
        IPage<Employee> page = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        String name = employeePageQueryDTO.getName();
        queryWrapper.like(name != null && !name.isEmpty(), Employee::getName, employeePageQueryDTO.getName())
                .orderByDesc(Employee::getUpdateTime);
        //查询结果
        IPage<Employee> employeeIPage = employeeMapper.selectPage(page, queryWrapper);
        return new PageResult(employeeIPage.getTotal(), employeeIPage.getRecords());
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        //封装数据
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        //更新数据
        employeeMapper.updateById(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        //进一步加强安全性
        employee.setPassword("****");
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        //封装数据为employee
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        //刷新最后修改员工和最后修改日期
//        employee.setUpdateUser(BaseContext.getCurrentId());
//        employee.setUpdateTime(LocalDateTime.now());
        //更新数据
        employeeMapper.updateById(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //获取用户
        Employee employee = employeeMapper.selectById(passwordEditDTO.getEmpId());
        //md5加密并密码校验
        String oldPass = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()).toUpperCase();
        if (!oldPass.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_ERROR);
        }
        //更改新密码
        employee.setPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()).toUpperCase());
        employeeMapper.updateById(employee);
    }
}
