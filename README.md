## jkami简介及特征

jkami是对jdbc轻量级的封装，不需要第三方jar包 实现 类似Hibernate实体维护和Mybaits SQL分离的两大优势。 具有以下特征:

* 1.映射关系不用设置xml，零配置
* 2.SQL语句和java代码的分离
* 3.可以自动生成SQL语句(依赖freemarker)
* 4.用户只需定义接口，spring AOP自动生成实现代理类
* 5.集成spring的事务管理
* 6.支持多数据源
* 7.SQL标签支持freemarker语法更灵活
* 8.自动识别分页查询
* 9.支持实体的懒加载
* 10.支持实体的级联查询（可配置查询深度）

## Quick Start
	 <dependency>
	    <groupId>com.github.lftao</groupId>
	    <artifactId>jkami</artifactId>
	    <version>1.0.1</version>
	</dependency>
## spring config
	 <bean class="com.javatao.jkami.spring.MapperScannerConfigurer">
        <property name="dataSourceId" value="dataSource1" />
        <property name="basePackage" value="jkami.demo.dao" />
        <property name="dbType" value="mysql" />
        <property name="lazybean" value="true" />
        <!-- 默认对应dao同级下的sql文件夹 -->
        <!--<property name="sqlpath" value="/sql/" /> -->
	 </bean>

### 接口实体[User.java]
	@Depth(2)//嵌套实体查询深度，可不填写,默认为2层
	@Table("tb_user")//对象表名
	public class User implements Serializable {
	    private static final long serialVersionUID = 1208054851593657045L;
	  
	    @Key
	    private Long id;
	    private String name;
	    private Integer age;
	    private Date birthday;
	    // 默认是字段的 小驼峰命名规则 nick_name
	    private String nickName;
	    
	    // 对应数据库字段
	    @Column("tel")
	    private String mobile;
	    
	    // 级联查询(如果开启懒加载只在第一次get时候读取) > 可选参数自身实体
	    @ResultType(UserOrder.class)
	    @Sql("select tb.* from tb_user_order tb where tb.user_id = :id")
	    private List<UserOrder> userOrder;
		 ....
	
	}


### 接口定义[IUserDao.java]
    
    @KaMiDao
	 @ResultType(User.class)
	 public interface IUserDao{
	 
	    User findUserByMobile(@Param("tel") Long mobile);
	     
	    List<User> findListByName(@Param("name") String name);
	
	    // :age or ${age}
	    @ResultType(Long.class)
	    @Sql("select count(1) from tb_user where age > :age ")
	    Long countAge(@Param("age") int age);
	
	   
	    @ResultType(Map.class)
	    List<Map<String, Object>> findMapByName(@Param("name") String name);
	
	    
	    @PageQuery
	    Page<User> findMyPage(@Param("map") Map<String, Object> map);
    }
 ### 接口定义[IUserOrderDao.java]
 
	@KaMiDao
	public interface IUserOrderDao {
	    // 执行更新插入，需要返回值
	    @ExecuteUpdate
	    int[] insertOrders(@Param("orders") List<UserOrder> orders);
	
	    // 执行更新插入，不需要返回值 可以不用 @ExecuteUpdate
	    void insertOrdersVoid(@Param("orders") List<UserOrder> orders);
	}
 
    
    
### SQL文件[findMapByName.sql]
	select
		*
	from
		tb_user
	where
		name = :name


### SQL文件[findMyPage.sql]
	select
		*
	from
		tb_user
	where
		1=1
	<#if map["name"]??>
	    and name like '%${map["name"]}%'
	</#if>
	<#if map["id"]??>
	    and id = '${map["id"]}'
	</#if>
	<#if id??>
	    and id = '${id}'
	</#if>

### SQL文件[insertOrders.sql]

	<#list orders as os>
	insert into tb_user_order(name,user_id,create_date) values('${os.name}',${os.user.id!os.userId},'${os.createDate?string("yyyy-MM-dd HH:mm:ss")}');
	</#list>

### 测试代码
 	https://github.com/lftao/jkami-demo
