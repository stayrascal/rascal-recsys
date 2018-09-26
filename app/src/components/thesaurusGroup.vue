<template>
  <v-layout justify-start row wrap>
    <v-flex xs10>
      <v-card>
        <v-card-text>
          <div class="text-xs-left">
            <transition-group name="list">
              <v-chip v-for="word in words" :key="word" :close="editable"
                      :color="chooseColor(word)"
                      text-color="white"
                      @input="remove(word)"
              >
                {{word}}
              </v-chip>
            </transition-group>
          </div>
        </v-card-text>
      </v-card>
    </v-flex>
    <v-flex xs1>
      <transition name="fade">
        <v-tooltip right>
          <transition name="fade" slot="activator">
            <v-btn v-if="editable && words.length > 0" medium color="red" @click="clearWords" round dark>
              <v-icon>clear</v-icon>
            </v-btn>
          </transition>
          <span>删除同义词组</span>
        </v-tooltip>
      </transition>
    </v-flex>
  </v-layout>
</template>

<script>

  export default {
    name: "thesaurusGroup",
    props: {
      inputs: Array,
      groupId: String,
      words: Array,
      editable: Boolean
    },
    methods: {
      chooseColor(word) {
        return this.inputs.indexOf(word) !== -1 ? 'primary' : 'secondary'
      },
      /**
       * 删除同义词组
       */
      clearWords() {
        this.$emit("clear", this.groupId)
      },
      /**
       * 从同义词组中移除单词
       * @param word  单词
       */
      remove(word) {
        // 若同义词组中仅有一个单词，则与删除同义词组相同
        if (this.words.length === 1) {
          this.clearWords()
        } else {
          this.$emit("remove", {groupId: this.groupId, word: word})
        }
      },
    }
  }
</script>

<style scoped>
  .fade-enter-active, .fade-leave-active {
    transition: all .5s;
  }

  .fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */
  {
    transform: scale(0);
  }

  .list-item {
    display: inline-block;
    margin-right: 10px;
  }
  .list-enter-active, .list-leave-active {
    transition: all 0.5s;
  }
  .list-enter, .list-leave-to
    /* .list-leave-active for below version 2.1.8 */ {
    opacity: 0;
    transform: translateX(30px);
  }
</style>
