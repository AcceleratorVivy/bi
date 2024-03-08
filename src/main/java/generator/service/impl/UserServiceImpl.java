package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mika.bi.model.entity.User;
import generator.service.UserService;
import com.mika.bi.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author weiyishen
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-03-08 17:36:46
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




