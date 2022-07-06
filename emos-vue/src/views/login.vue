<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">轻云在线办公系统</h3>
      <el-form-item prop="username">
        <el-input
          v-model="username"
          type="text"
          auto-complete="off"
          placeholder="账号"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="login()"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input
          v-model="code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="login()"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="login()"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <a href="https://beian.miit.gov.cn/" target="_blank">皖ICP备2021017894号-1</a>
    </div>
  </div>
</template>

<script>
import { isUsername, isPassword } from '../utils/validate.js';
import 'element-plus/lib/theme-chalk/display.css';
import router from '../router/index.js';
export default {
	data: function() {
		return {
			username: 123456,
			password: 123456,
			code: "",
			uuid: "",
			codeUrl: "",
		};
	},
	
	created() {
		this.getCode();
	},

	methods: {
		
		getCode: function() {
			let that = this;
			that.$http('captchaImage', 'GET', null, true, function(resp) {
			    if (resp.code == 200) {
			        that.uuid = resp.uuid;
					that.codeUrl = "data:image/gif;base64," + resp.img;
			    } else {
			        that.$message({
			            message: '验证码生成失败',
			            type: 'error',
			            duration: 1200
			        });
			    }
			});
		},
		
		login: function() {
		    let that = this;
		    if (!isUsername(that.username)) {
		        that.$message({
		            message: '用户名格式错误',
		            type: 'error',
		            duration: 1200
		        });
		    } else if (!isPassword(that.password)) {
		        that.$message({
		            message: '密码格式错误',
		            type: 'error',
		            duration: 1200
		        });
		    } else if(that.code.length == 0 || that.code == ''){
				that.$message({
				    message: '请输入验证码',
				    type: 'error',
				    duration: 1200
				});
			} else {
		        let data = { username: that.username, password: that.password, 
							code: that.code, uuid: that.uuid};
		        //发送登陆请求
		        that.$http('user/login', 'POST', data, true, function(resp) {
		            if (resp.result) {
		                //在浏览器的storage中存储用户权限列表，这样其他页面也可使用storage中的数据，实现共享
		                let permissions = resp.permissions;
		                //取出Token令牌，保存到storage中
		                let token = resp.token;
		                localStorage.setItem('permissions', permissions);
		                localStorage.setItem('token', token);
		                //让路由跳转页面，这里的Home是home.vue页面的名字
		                router.push({ name: 'Home' });
		            } else {
		                that.$message({
		                    message: '用户名或密码错误',
		                    type: 'error',
		                    duration: 1200
		                });
		            }
		        });
		    }
		},
	}
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/login/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
</style>
