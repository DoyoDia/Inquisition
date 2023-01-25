package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.mapper.AdminMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.entity.AdminEntity;
import moe.dazecake.inquisition.utils.Encoder;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "管理员接口")
@ResponseBody
@RestController
public class AdminController {

    private static final String salt = "arklightscloud";

    @Resource
    AdminMapper adminMapper;

    @Resource
    ProUserMapper proUserMapper;

    @Operation(summary = "管理员登陆")
    @PostMapping("/adminLogin")
    public Result<HashMap<String, String>> adminLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("username or password is null")
                    .setData(null);
        }

        var admin = adminMapper.selectOne(
                Wrappers.<AdminEntity>lambdaQuery()
                        .eq(AdminEntity::getUsername, username)
                        .eq(AdminEntity::getPassword, Encoder.MD5(password + salt))
        );

        if (admin != null) {
            return result.setCode(200)
                    .setMsg("login success")
                    .setData(new HashMap<>() {
                        {
                            put("token", JWTUtils.generateTokenForAdmin(admin));
                        }
                    });
        } else {
            return result.setCode(404)
                    .setMsg("Account does not exist")
                    .setData(null);
        }
    }

    @Login
    @Operation(summary = "为pro_user增加余额")
    @PostMapping("/addBalanceForProUser")
    public Result<String> addBalanceForProUser(Long id, Integer balance) {
        Result<String> result = new Result<>();

        var proUser = proUserMapper.selectById(id);
        if (proUser != null) {
            proUser.setBalance(proUser.getBalance() + balance);
            proUserMapper.updateById(proUser);

            result.setCode(200);
            result.setMsg("success");

        } else {
            result.setCode(403);
            result.setMsg("Unable to add balance to a non-existent account");
        }

        return result;
    }
}
