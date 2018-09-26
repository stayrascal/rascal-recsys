<template>
  <v-app id="inspire">
    <v-navigation-drawer fixed v-model="drawer" app dark>
      <v-list dense>
        <v-list-tile @click="toggleView('/component')">
          <v-list-tile-action>
            <v-icon>extension</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>构建检索与管理</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile @click="toggleView('/item')">
          <v-list-tile-action>
            <v-icon>event</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>Item检索与管理</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-group prepend-icon="book" no-action>
          <v-list-tile slot="activator">

            <v-list-tile-content>
              <v-list-tile-title>字典管理</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="toggleView('/jieba')">
            <v-list-tile-action>
              <v-icon>card_membership</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>结巴分词字典管理</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="toggleView('/thesaurus')">
            <v-list-tile-action>
              <v-icon>card_giftcard</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>同义词字典管理</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
        </v-list-group>
        <v-list-tile @click="toggleView('/recommend')">
          <v-list-tile-action>
            <v-icon>radio_button_checked</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>Item推荐</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-group prepend-icon="history" no-action>
          <v-list-tile slot="activator">
            <v-list-tile-content>
              <v-list-tile-title>历史纪录管理</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="toggleView('/history/add')">
            <v-list-tile-action>
              <v-icon>playlist_add</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>历史纪录添加</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="toggleView('/history/statistics')">
            <v-list-tile-action>
              <v-icon>multiline_chart</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>历史纪录统计</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
        </v-list-group>

      </v-list>
    </v-navigation-drawer>
    <v-toolbar dark flat fixed app>
      <v-toolbar-side-icon @click.stop="drawer = !drawer"/>
      <v-toolbar-title>检索与推荐系统</v-toolbar-title>
    </v-toolbar>
    <v-content>
      <v-container fluid fill-height>
        <!--内容视图-->
        <keep-alive>
          <router-view></router-view>
        </keep-alive>
      </v-container>
    </v-content>
    <v-footer app >
      <span class="black--text px-3">&copy; 2018 - stayrascal</span>
    </v-footer>
  </v-app>
</template>

<script>

  export default {
    name: 'app',
    data: () => ({
      drawer: null
    }),
    props: {
      source: String
    },
    methods: {
      toggleView(path) {
        this.$router.push(path)
      }
    }
  }
</script>

<style scoped>

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
    opacity: 0;
  }
</style>
