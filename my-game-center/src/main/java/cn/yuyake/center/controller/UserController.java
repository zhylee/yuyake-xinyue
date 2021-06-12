package cn.yuyake.center.controller;

import cn.yuyake.center.service.UserLoginService;
import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.error.IServerError;
import cn.yuyake.common.utils.JWTUtil;
import cn.yuyake.db.entity.UserAccount;
import cn.yuyake.http.MessageCode;
import cn.yuyake.http.request.LoginParam;
import cn.yuyake.http.response.LoginResult;
import cn.yuyake.http.response.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // RestController = Controller + ResponseBody
@RequestMapping("/request")
public class UserController {

    @Autowired
    private UserLoginService userLoginService;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping(MessageCode.USER_LOGIN)
    public ResponseEntity<LoginResult> login(@RequestBody LoginParam loginParam) {
        // 检测请求参数的合法性
        loginParam.checkParam();
        IServerError serverError = userLoginService.verifySdkToken(loginParam.getOpenId(), loginParam.getToken());
        // 检测第三方SKD是否合法，如果没有接入SDK，可以去掉此步
        if (serverError != null) {
            // 如果有错误，抛出异常，由全局异常捕获类处理
            throw GameErrorException.newBuilder(serverError).build();
        }
        UserAccount userAccount = userLoginService.login(loginParam);
        // 执行登录操作
        LoginResult loginResult = new LoginResult();
        loginResult.setUserId(userAccount.getUserId());
        // 这里使用JWT生成token，token中包括openId.userId
        String token = JWTUtil.getUserToken(userAccount.getOpenId(), userAccount.getUserId());
        loginResult.setToken(token);
        logger.debug("user {} 登陆成功", userAccount);
        return new ResponseEntity<>(loginResult);
    }
}
