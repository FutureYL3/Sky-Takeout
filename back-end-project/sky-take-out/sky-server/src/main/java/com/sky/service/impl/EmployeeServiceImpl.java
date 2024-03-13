package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

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
        // TODO （已完成）后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (Objects.equals(employee.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        // 拷贝属性
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置默认密码，默认为123456的md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置账号状态，默认为1
        employee.setStatus(StatusConstant.ENABLE);

        // 设置创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置更新和创建用户操作人的id
        // TODO （已完成）后期更改为当前操作人的id
        Long id = BaseContext.getCurrentId();
        employee.setCreateUser(id);
        employee.setUpdateUser(id);

        // 将员工插入到数据库中
        save(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        int page = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        String name = employeePageQueryDTO.getName();

        IPage<Employee> employeePage = new Page<Employee>().setPages(page).setSize(pageSize);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>().like(name != null, Employee::getName, name);

        IPage<Employee> result = employeeMapper.selectPage(employeePage, wrapper);
        long total = result.getTotal();
        List<Employee> records = result.getRecords();

        return new PageResult(total, records);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        LambdaUpdateWrapper<Employee> wrapper = new LambdaUpdateWrapper<Employee>()
                .eq(Employee::getId, id)
                .set(Employee::getStatus, status);
        update(wrapper);
    }

    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        LambdaUpdateWrapper<Employee> wrapper = new LambdaUpdateWrapper<Employee>()
                .eq(Employee::getId, employeeDTO.getId())
                .set(Employee::getUsername, employeeDTO.getUsername())
                .set(Employee::getName, employeeDTO.getName())
                .set(Employee::getPhone, employeeDTO.getPhone())
                .set(Employee::getSex, employeeDTO.getSex())
                .set(Employee::getIdNumber, employeeDTO.getIdNumber())
                .set(Employee::getUpdateTime, LocalDateTime.now())
                .set(Employee::getUpdateUser, BaseContext.getCurrentId());

        update(wrapper);

    }


}
