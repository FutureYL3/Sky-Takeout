package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.sky.result.Result.success;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);
        // 将前端传递的密码加密为MD5密文，与后端数据库比较
        employeeLoginDTO.setPassword(DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes()));

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return success();
    }


    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        // 日志记录
        log.info("增加员工：{}", employeeDTO);

        // 调用service插入
        employeeService.save(employeeDTO);

        return success();
    }


    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 日志记录
        log.info("查询员工参数：{}", employeePageQueryDTO);

        // 调用service查询
        PageResult data = employeeService.pageQuery(employeePageQueryDTO);

        // 返回数据
        return Result.success(data);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result page(@PathVariable Integer status, Long id) {
        // 日志记录
        log.info("修改id为{}的员工的状态为{}", id, status);

        // 调用service修改状态
        Employee employee = Employee.builder().id(id).status(status).build();
        employeeService.updateStatus(employee);

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getByid(@PathVariable Long id) {
        // 日志记录
        log.info("查询id为{}的员工", id);

        // 调用service查询员工
        Employee employee = employeeService.getById(id);
        // 重新设置密码，保证安全性
        employee.setPassword("******");

        return Result.success(employee);
    }

    @PutMapping
    @ApiOperation("修改员工信息")
    public Result updateEmployee(@RequestBody Employee employee) {
        // 日志记录
        log.info("修改id为{}的员工信息为：{}", employee.getId(), employee);

        // 调用service修改员工信息
        employeeService.updateEmployee(employee);

        return Result.success();
    }

}
