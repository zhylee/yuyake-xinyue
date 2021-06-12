package cn.yuyake.center.controller;

import cn.yuyake.center.service.PlayerService;
import cn.yuyake.center.service.UserLoginService;
import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.error.IServerError;
import cn.yuyake.common.error.TokenException;
import cn.yuyake.common.utils.JWTUtil;
import cn.yuyake.db.entity.Player;
import cn.yuyake.db.entity.UserAccount;
import cn.yuyake.error.GameCenterError;
import cn.yuyake.http.MessageCode;
import cn.yuyake.http.request.CreatePlayerParam;
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

import javax.servlet.http.HttpServletRequest;

@RestController // RestController = Controller + ResponseBody
@RequestMapping("/request")
public class UserController {

    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private PlayerService playerService;

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

    @PostMapping(MessageCode.CREATE_PLAYER)
    public ResponseEntity<Player> createPlayer(@RequestBody CreatePlayerParam param, HttpServletRequest request) {
        param.checkParam();
        // 从http 包头里面获取 token 的值
        String token = request.getHeader("token");
        if (token == null) {
            throw GameErrorException.newBuilder(GameCenterError.TOKEN_FAILED).build();
        }
        JWTUtil.TokenBody tokenBody;
        try {
            tokenBody = JWTUtil.getTokenBody(token);// 从加密的token中获取明文信息
        } catch (TokenException e) {
            throw GameErrorException.newBuilder(GameCenterError.TOKEN_FAILED).build();
        }
        String openId = tokenBody.getOpenId();
        // 使用网关之后，就可以在这里直接获取openId，网关那边会自动验证权限，如果没有使用网关，需要打开上面注释，并注释掉下面这行代码。
        // String openId = userLoginService.getOpenIdFromHeader(request);
        UserAccount userAccount = userLoginService.getUserAccountByOpenId(openId).get();
        String zoneId = param.getZoneId();
        Player player = userAccount.getPlayerInfo().get(zoneId);
        if (player == null) { // 如果没有创建角色，创建角色
            player = playerService.createPlayer(param.getZoneId(), param.getNickName());
        }
        userAccount.getPlayerInfo().put(zoneId, player);
        ResponseEntity<Player> response = new ResponseEntity<>(player);
        return response;
    }
}
