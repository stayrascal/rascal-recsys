import Vue from 'vue'
import App from './App.vue'
import VueResource from 'vue-resource'
import VueRouter from 'vue-router'
import Vuetify from 'vuetify'
import VueRx from 'vue-rx'
import { Observable } from 'rxjs/observable'
import { Subject } from 'rxjs/Subject'

import Button from 'element-ui'
import 'vuetify/dist/vuetify.min.css'
import 'material-design-icons-iconfont/dist/material-design-icons.scss'
import Item from './components/itemManage'
import CompManage from './components/compManage'
import JiebaManage from './components/jiebaManage'
import ThesaurusManage from './components/thesaurusManage'
import RecommendManage from './components/compRecommendManage'
import HistoryStatistics from './components/historyStatistics'
import HistoryAdd from './components/historyAdd'
import Login from './components/login'

Vue.config.debug = true;

Vue.use(VueResource);
Vue.use(VueRouter);
Vue.use(Vuetify);
Vue.use(Button);
Vue.use(VueRx, {
  Observable,
  Subject
});

const router = new VueRouter({
  mode: 'history',
  base: __dirname,
  routes: [
    { path: '/component', component: CompManage },
    { path: '/jieba', component: JiebaManage },
    { path: '/thesaurus', component: ThesaurusManage },
    { path: '/recommend', component: RecommendManage },
    { path: '/recommend/:uuid/:compName', component: RecommendManage },
    { path: '/history/statistics', component: HistoryStatistics },
    { path: '/history/add', component: HistoryAdd },
    { path: '', component: CompManage },
    { path: '/item', component: Item },
    { path: '/login', component: Login }
  ]
});

new Vue({
  el: '#app',
  router: router,
  render: h => h(App),
  // watch: {
  //   "$route": 'checkLogin'
  // },
  created() {
    this.checkLogin();
  },
  methods: {
    checkLogin() {
      if (!this.getCookie('session')) {
        this.$router.push('/login');
      } else {
        this.$router.push('/component');
      }
    },
    getCookie(name) {
      var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
      if (arr = document.cookie.match(reg))
        return (arr[2]);
      else
        return null;
    },
    setCookie(c_name, value, expiredays) {
      var exdate = new Date();
      exdate.setDate(exdate.getDate() + expiredays);
      document.cookie = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
    },
    delCookie(name) {
      var exp = new Date();
      exp.setTime(exp.getTime() - 1);
      var cval = getCookie(name);
      if (cval != null)
        document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
    }
  }
});
