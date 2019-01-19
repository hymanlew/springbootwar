package com.hyman.springbootwar;

import com.hyman.springbootwar.entity.User;
import com.hyman.springbootwar.service.UserService;
import com.hyman.springbootwar.util.LogUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootwarApplicationTests {

	@Resource
	private DataSource dataSource;

	@Test
	public void test0() {
		try {
			LogUtil.logger.info("====== "+dataSource.getClass());

			Connection connection = dataSource.getConnection();
			LogUtil.logger.info("====== "+connection);
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 是利用原生态的方式，Spring的 JdbcTemplate是自动配置的，你可以直接使用 @Autowired 来注入到你自己的bean中来使用。
	 */
	@Resource
	private UserService userService;

	@Before
	public void out(){
		System.out.println("准备测试！");
	}

	@Test
	public void test() {
		//userService.create("a",1);
		//userService.create("b",2);
		//userService.create("c",3);

		/**
		 * Assert 断言：
		 * 编写代码时，我们总是会做出一些假设，断言就是用于在代码中捕捉这些假设，可以将断言看作是异常处理的一种高级形式。
		 *
		 * 断言表示为一些布尔表达式，程序员相信在程序中的某个特定点该表达式值为真。可以在任何时候启用和禁用断言验证，因此
		 * 可以在测试时启用断言，而在部署时禁用断言。同样程序投入运行后，最终用户在遇到问题时可以重新启用断言。
		 *
		 * 使用断言可以创建更稳定，品质更好且不易于出错的代码。当需要在一个值为FALSE时中断当前操作的话，可以使用断言。
		 * 单元测试必须使用断言（Junit/JunitX）。
		 *
		 * 断言特性：
		 * 前置条件断言：代码执行之前必须具备的特性，
		 * 后置条件断言：代码执行之后必须具备的特性，
		 * 前后不变断言：代码执行前后不能变化的特性，
		 * 如果结果为 true 则才会走下一步，如果为 false 则报错 AssertError。
		 *
		 * assertEqual(a,b，[msg='测试失败时打印的信息'])： 断言a和b是否相等，相等则测试用例通过。
		 * assertNotEqual(a,b，[msg='测试失败时打印的信息'])： 断言a和b是否相等，不相等则测试用例通过。
		 * assertTrue(x，[msg='测试失败时打印的信息'])： 断言x是否True，是True则测试用例通过。
		 *
		 * assertFalse(x，[msg='测试失败时打印的信息'])： 断言x是否False，是False则测试用例通过。
		 * assertIs(a,b，[msg='测试失败时打印的信息'])： 断言a是否是b，是则测试用例通过。
		 *
		 * assertNotIs(a,b，[msg='测试失败时打印的信息'])： 断言a是否是b，不是则测试用例通过。
		 * assertIsNone(x，[msg='测试失败时打印的信息'])： 断言x是否None，是None则测试用例通过。
		 *
		 * assertIsNotNone(x，[msg='测试失败时打印的信息'])： 断言x是否None，不是None则测试用例通过。
		 * assertIn(a,b，[msg='测试失败时打印的信息'])： 断言a是否在b中，在b中则测试用例通过。
		 *
		 * assertNotIn(a,b，[msg='测试失败时打印的信息'])： 断言a是否在b中，不在b中则测试用例通过。
		 * assertIsInstance(a,b，[msg='测试失败时打印的信息'])： 断言a是是b的一个实例，是则测试用例通过。
		 *
		 * assertNotIsInstance(a,b，[msg='测试失败时打印的信息'])： 断言a是是b的一个实例，不是则测试用例通过。
		 *
		 */
		// 因为原先表中已经有三个用户了
		//Assert.assertEquals(6,userService.getAllUsers().intValue());

		userService.deleteByName("c");
		//Assert.assertEquals(5,userService.getAllUsers().intValue());
	}


	///**
	// * Spring-data-jpa的出现正可以让这样一个已经很“薄”的数据访问层变成只是一层接口的编写方式。
	// * 我们只需要通过编写一个继承自 JpaRepository的接口就能完成数据访问，Spring-data-jpa 依赖于 Hibernate。
	// */
	//@Resource
	//private UserRepository userRepository;
	//
	//@Test
	//public void test1(){
	//	//由于 JpaRepository 已经自身集成了增删查改四个方法，所以直接使用即可。
	//	//userRepository.save(new User(null,"A2",10));
	//	//userRepository.save(new User(null,"B2",20));
	//	//userRepository.save(new User(null,"C2",30));
	//
	//	Assert.assertEquals(11,userRepository.count());
	//	Assert.assertEquals(20,userRepository.findUser("B2").getAge().intValue());
	//	Assert.assertEquals(30,userRepository.findByNameAndAge("C2",30).getAge().intValue());
	//
	//	userRepository.delete(userRepository.findByName("A2"));
	//
	//	// 此方法 size() 是 JpaRepository 的方法，当前实现的接口不可用。
	//	//Assert.assertEquals(10,userRepository.findAll().size());
	//	Assert.assertEquals(10,userRepository.count());
	//}


	/**
	 * 自动配置的 StringRedisTemplate 对象进行Redis的读写操作，该对象从命名中就可注意到支持的是String类型。如果有使用过
	 * spring-data-redis 的开发者一定熟悉 RedisTemplate<K, V> 接口，StringRedisTemplate 就相当于 RedisTemplate<String, String>
	 * 的实现。
	 */
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Test
	public void test2(){
		stringRedisTemplate.opsForValue().set("aaa","111");
		Assert.assertEquals("111",stringRedisTemplate.opsForValue().get("aaa"));
	}


	/**
	 * 除了String类型，还可以存储对象，使用类似 RedisTemplate<String, User> 来初始化并进行操作。
	 * converter ：变流器，转化器；
	 *
	 * Redis五大类型:字符串（String）、哈希/散列（Hash）、列表（List）、集合（Set）、有序集合（sorted set/Zset），相应的操作方法：
	 *
	 * redisTemplate.opsForValue();		//操作字符串
	 * redisTemplate.opsForHash();		//操作hash
	 * redisTemplate.opsForList();		//操作list
	 * redisTemplate.opsForSet();		//操作set
	 * redisTemplate.opsForZSet();		//操作有序set
	 *
	 * 并且在每种类型操作方法之内还有更加细的具体操作方法，如 set，get 这种。
	 */
	@Resource
	private RedisTemplate<String,User> redisTemplate;
	@Resource
	private RedisTemplate<String,User> myRedisTemplate;

	@Test
	public void test3(){
		/**
		 * 在 redisConfig 包中的类是在低版本 redis-starter 中，对对象的存储需要自定义序列化的。但在高版本中，全部是自动配置的。
		 * 保存对象的机制，默认是使用 JDK 的序列化机制。
		 */
		User user = new User("man", 20);
		redisTemplate.opsForValue().set(user.getName(),user);

		user = new User("girl", 30);
		redisTemplate.opsForValue().set(user.getName(),user);

		Assert.assertEquals(20,redisTemplate.opsForValue().get("man").getAge().intValue());
		Assert.assertEquals(30,redisTemplate.opsForValue().get("girl").getAge().intValue());


		user = new User("jman", 20);
		myRedisTemplate.opsForValue().set(user.getName(),user);

		user = new User("jgirl", 30);
		myRedisTemplate.opsForValue().set(user.getName(),user);
		LogUtil.logger.info("==== 存储对象成功 ====");
	}


}

