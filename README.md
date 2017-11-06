# mybatisPlugin
mybatis中，快捷查看对应sql的小插件

### 适用范围
#### 文件起名相对统一及dao与service中方法名一致的web项目：
#### 如  xxxDao*.java、xxxService*.java、xxxDao*.xml、xxxMapper*.xml
 ` UserDao.java
    
    public interface UserDao {
    
        int insert(User record);
        
        int countUser(UserVo userVo);
    }
    
 `

` UserService.java
   
    public class UserService {
   
       @Autowired
       UserDao dao;
   
       int insert(User user){
            return dao.insert(user);
       }
    
       int countUser(UserVo userVo){
            return dao.countUser(userVo);
       }
    }
 
`
