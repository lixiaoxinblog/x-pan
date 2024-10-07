package com.xiaoxin.pan.server.modules.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.xiaoxin.pan.core.utils.IdUtil;
import com.xiaoxin.pan.server.modules.file.context.QueryUploadedChunksContext;
import com.xiaoxin.pan.server.modules.file.entity.XPanFileChunk;
import com.xiaoxin.pan.server.modules.file.service.XPanFileChunkService;
import com.xiaoxin.pan.server.modules.file.service.XPanUserFileService;
import com.xiaoxin.pan.server.modules.file.vo.UploadedChunksVO;
import com.xiaoxin.pan.server.modules.user.context.UserRegisterContext;
import com.xiaoxin.pan.server.modules.user.service.XPanUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest(classes = com.xiaoxin.pan.server.XPanServerLauncher.class)
public class FileTest {

    @Autowired
    private XPanUserService xPanUserService;
    @Autowired
    private XPanFileChunkService xPanFileChunkService;
    @Autowired
    private XPanUserFileService iUserFileService;


    /**
     * 测试查询用户已上传的文件分片信息列表成功
     */
    @Test
    public void testQueryUploadedChunksSuccess() {
        Long userId = register();

        String identifier = "123456789";

        XPanFileChunk record = new XPanFileChunk();
        record.setId(IdUtil.get());
        record.setIdentifier(identifier);
        record.setRealPath("realPath");
        record.setChunkNumber(1);
        record.setExpirationTime(DateUtil.offsetDay(new Date(), 1));
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        boolean save = xPanFileChunkService.save(record);
        Assert.isTrue(save);

        QueryUploadedChunksContext context = new QueryUploadedChunksContext();
        context.setIdentifier(identifier);
        context.setUserId(userId);
        UploadedChunksVO vo = iUserFileService.getUploadedChunks(context);
        Assert.notNull(vo);
        Assert.notEmpty(vo.getUploadedChunks());
    }

    /**
     * 用户注册
     *
     * @return 新用户的ID
     */
    private Long register() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = xPanUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);
        return register;
    }


    private final static String USERNAME = "xiaoxin11111";
    private final static String PASSWORD = "123456789";
    private final static String QUESTION = "question";
    private final static String ANSWER = "answer";

    /**
     * 构建注册用户上下文信息
     *
     * @return
     */
    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context = new UserRegisterContext();
        context.setUsername(USERNAME);
        context.setPassword(PASSWORD);
        context.setQuestion(QUESTION);
        context.setAnswer(ANSWER);
        return context;
    }

    /**
     * 测试加解密
     */
    @Test
    public void testEncryptAndDecrypt() {
        Long content = 1837412993414815744L;
        String encrypt = IdUtil.encrypt(1837412993414815744L);
        System.out.println(encrypt);
        Long decrypt = IdUtil.decrypt(encrypt);
        System.out.println(decrypt);

        Assert.isTrue(content.equals(decrypt));
    }


}
