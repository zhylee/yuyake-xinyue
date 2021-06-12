package cn.yuyake.center.service;

import cn.yuyake.common.error.IServerError;
import cn.yuyake.dao.UserAccountDao;
import cn.yuyake.db.entity.UserAccount;
import cn.yuyake.http.request.LoginParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLoginService {

    @Autowired
    private UserAccountDao userAccountDao;

    private final static Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    // 这里调用sdk服务端验证接口
    public IServerError verifySdkToken(String openId, String token) {
        return null;
    }

    public UserAccount login(LoginParam loginParam) {
        String openId = loginParam.getOpenId();
        openId = openId.intern();// 将openId放入到常量池
        synchronized (openId) {// 对openId加锁，防止用户单击注册多次
            Optional<UserAccount> op = userAccountDao.findById(openId);
            return op.orElseGet(() -> this.register(loginParam)); // 用户不存在，自动注册
        }
    }

    private UserAccount register(LoginParam loginParam) {
        // 使用redis自增保证userId全局唯一
        long userId = userAccountDao.getNextUserId();
        UserAccount userAccount = new UserAccount();
        userAccount.setOpenId(loginParam.getOpenId());
        userAccount.setCreateTime(System.currentTimeMillis());
        userAccount.setUserId(userId);
        userAccountDao.saveOrUpdate(userAccount, userAccount.getOpenId());
        logger.debug("user {} 注册成功", userAccount);
        return userAccount;
    }
}
