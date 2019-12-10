package com.hyman.springbootwar.config;

import com.hyman.springbootwar.util.LogUtil;
import com.hyman.springbootwar.util.RequestWrapper;
//import org.apache.catalina.filters.RemoteIpFilter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SpringBoot默认使用Tomcat作为嵌入式的Servlet容器：
 *
 * 1，修改内置 server有关的配置，可以查看系统配置类 ServerProperties（也是EmbeddedServletContainerCustomizer），即：server.xxx
 *  （通用的 Servlet容器设置），server.tomcat.xxx（Tomcat的设置），servlet.context-path='/'（当前项目路径设置），等等。
 *
 * 2，编写一个EmbeddedServletContainerCustomizer：嵌入式的Servlet容器的定制器，来修改Servlet容器的配置。一定要将这个定制器加入到
 *   容器中（这是老版本的方法，现在不可用）。
 *
 * 3，由于 SpringBoot默认是以 jar包的方式启动嵌入式的 Servlet容器来启动 SpringBoot 的 web 应用，没有 web.xml文件。所以注册三大组
 *   件时是用：ServletRegistrationBean，FilterRegistrationBean，ServletListenerRegistrationBean。
 *
 * 4，SpringBoot自动配置 SpringMVC时，会自动注册 MVC的前端控制器 DIspatcherServlet，即 DispatcherServletAutoConfiguration。它默
 *    认拦截 / 所有请求包括静态资源，但是不拦截jsp请求（/*会拦截jsp）。可以通过 server.servletPath 来修改默认拦截的请求路径。
 *
 * 5，springboot 还支持 Jetty（适合长连接，即实时聊天场景，），Undertow（不支持 JSP，但并发性好）两种 servlet 容器（见 pom 文件）。
 */
@Configuration
public class SeverConfiguration {

    @Bean
    public ServletRegistrationBean addServlet(){
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new
                MyServlet(),"/myServlet");
        // 设置启动加载顺序
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    public class MyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            LogUtil.logger.info("============= 自定义 servlet");
            super.doPost(req, resp);
        }
    }


    /**
     * 我们常常在项目中会使用 filters 用于录调用日志、排除有XSS威胁的字符、执行权限验证等等。
     * Spring Boot自动添加了 OrderedCharacterEncodingFilter 和 HiddenHttpMethodFilter，并且我们可以自定义Filter。
     *
     * 两个步骤：
     * 1，实现Filter接口，实现Filter方法
     * 2，添加@Configuration 注解，将自定义Filter加入过滤链
     *
     * Spring Boot、Spring Web和Spring MVC等其他框架，都提供了很多servlet 过滤器可使用，我们需要在配置文件中定义这些过滤器
     * 为bean对象。
     * Tomcat 8 提供了对应的过滤器：RemoteIpFilter。通过将 RemoteFilter 这个过滤器加入过滤器调用链即可使用它。
     */
    @Bean
    public RemoteIpFilter remoteIpFilter(){
        return new RemoteIpFilter();
    }

    @Bean
    public FilterRegistrationBean testFilterRegist(){
        FilterRegistrationBean regist = new FilterRegistrationBean();
        //注入过滤器
        regist.setFilter(new MyFilter());
        //拦截规则
        regist.addUrlPatterns("/*");
        //过滤器初始化参数
        regist.addInitParameter("name","value");
        //过滤器名称
        regist.setName("myfilter");
        //为 filter 设置排序值，让 spring 在注册 filter之前排序后再依次注册。过滤器顺序
        regist.setOrder(1);
        return regist;
    }

    public class MyFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        /**
         * 基于 SpringBoot的 maven项目，拦截器的使用很多时候是必不可少的，当有需要需要你对 body中的值进行校验，例如加密验签、防重复提交、内容校验等等。 
         * 当你在拦截器中通过 request.getInputStream();获取到body中的信息后，你会发现你在 controller中使用了 @RequestBody注解获取参数报如下错误：
         * java.io.IOException: Stream closed。
         *
         * 这是因为 @RequestBody 只能以流的方式读取，它是被放在内存中的，当流被读过一次后，再次读取时就会从上次读取的位置开始，那自然数据就不存在了，
         * 会导致会续无法处理，因此不能直接读流。
         * 为了解决这个问题，思路如下：
         * 1、读取流前先把流保存一下。
         * 2、使用过滤器拦截读取，再通过chain.doFilter(wrapper, response);将保存的流丢到后面程序处理。
         *
         * 在使用springMVC中，需要在过滤器中获取请求中的参数token，根据token判断请求是否合法。可以通过 requst.getParameter(key)方法获得参数值，但是
         * 这种方法有缺陷：它只能获取 POST 提交方式中的 Content-Type: application/x-www-form-urlencoded。
         *
         *
         * 但如果涉及到文件的上传操作，则上传文件的请求 content-type为：multipart/form-data；此种请求无法直接用 request.getParam(key)获取对应的属性值，
         * request中获取的属性值全部为空，无法正常获取；
         * 就需要借助 Spring框架中的 CommonsMultipartResolver.resolveMultipart(HttpServletRequest request) 将 request转为 MultipartHttpServletRequest，
         * 从而使用 getParameter(key)方法获取指定的值；
         * 在将对象转化完成后，要将转化完成的对象赋值给过滤链中的 request参数中，即如下代码中的 req = multiReq； 赋值完成很重要，否则在controller层中依旧
         * 无法获取其他参数。
         * 如果不需要在 filter中获取请求中的值，则无需额外的操作，因为在请求经过 springMVC框架后，框架会自动识别请求方式，如果是文件请求，会自动调用
         * CommonsMultipartResolver.resolveMultipart(HttpServletRequest request) 方法转化；
         */
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

            ServletRequest requestWrapper = null;

            // 因为 ServletRequest 与 HttpServletRequest 都是接口，所以可以使用 instanceof（判断的是其实现类）。
            if(servletRequest instanceof HttpServletRequest){

                HttpServletRequest request = (HttpServletRequest)servletRequest;
                System.out.println("============= 拦截 url："+request.getRequestURI());
                requestWrapper = new RequestWrapper(request);
            }
            if(requestWrapper == null){
                filterChain.doFilter(servletRequest,servletResponse);
            }else {
                filterChain.doFilter(requestWrapper,servletResponse);
            }
        }

        @Override
        public void destroy() {
        }
    }


    @Bean
    public ServletListenerRegistrationBean addListener(){
        ServletListenerRegistrationBean registrationBean = new ServletListenerRegistrationBean(new MyListener());
        return registrationBean;
    }

    public class MyListener implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent servletContextEvent) {
            LogUtil.logger.info("============= 自定义 listener ==== 服务器启动");
        }

        @Override
        public void contextDestroyed(ServletContextEvent servletContextEvent) {
            LogUtil.logger.info("============= 自定义 listener ==== 服务器关闭");
        }
    }
}
