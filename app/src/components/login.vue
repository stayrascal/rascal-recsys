<template>
  <v-layout justify-center row wrap>
    <v-flex xs12 sm8 text-xs-center class="px-2" justify-center row wrap>
      <v-flex>
        <v-card>
          <v-card-title primary-title>
            <h3>登录</h3>
          </v-card-title>
          <v-card-text>
            <v-form v-model="valid">
              <v-text-field
                label="用户名"
                v-model="user.username"
                :rules="nameRules"
                :counter="nameCharLen"
                required
              ></v-text-field>
              <v-text-field
                label="密码"
                v-model="user.password"
                required
              ></v-text-field>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" :disabled="!valid" @click="login">登录</v-btn>
          </v-card-actions>
        </v-card>
      </v-flex>
    </v-flex>
  </v-layout>
</template>

<script>
  import { USER } from '../configs/srapp.api'

  export default {
    name: 'Login',
    data() {
      return {
        isLoging: false,
        valid: false,
        user: {
          username: '',
          password: ''
        },
        nameCharLen: 25,
        nameRules: [
          v => !!v || '用户名不能为空',
          v => v.length <= this.nameCharLen || '用户名长度过长'
        ]
      }
    },
    methods: {
      login() {
        if (this.account != '' && this.password != '') {
          this.toLogin();
        }
      },
      toLogin() {
        this.isLoging = true;
        this.$http.post(USER + '/login', {
          params: {
            id: this.account,
          }
        }).then((response) => {
          if (response.status === 200){
            let expireDays = 1000 * 60 * 60 * 24 * 15;
            this.setCookie('session', response.data.session, expireDays);
            this.$router.push('/component');
          }
        }, error => {
          //Error
        })
      },
      setCookie(c_name, value, expiredays) {
        var exdate = new Date();
        exdate.setDate(exdate.getDate() + expiredays);
        document.cookie = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
      },
    }
  }
</script>

<style scoped>

</style>
