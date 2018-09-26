<template>
  <v-layout justify-start>
    <v-flex xs10>
      <v-select
        v-model="inputs"
        label="输入一个或一组词"
        chips
        tags
        solo
        prepend-icon="mode_edit"
        append-icon=""
        clearable
      >
        <template slot="selection" slot-scope="data">
          <v-chip
            :selected="data.selected"
            :color="chooseColor(data.item)"
            text-color="white"
          >
            <strong>{{ data.item }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-select>
    </v-flex>
    <v-flex xs1>
      <v-tooltip right>
        <transition name="fade" slot="activator">
          <v-btn v-if="addable && !combinable" color="green" round dark medium
                 @click="sendAddEvent"
          >
            <v-icon>add</v-icon>
          </v-btn>
          <v-btn v-if="combinable" color="green" round dark medium
                 @click="sendCombineEvent">
            <v-icon>add_circle</v-icon>
          </v-btn>
        </transition>
        <span>添加同义词组</span>
      </v-tooltip>
    </v-flex>
  </v-layout>
</template>

<script>
  export default {
    name: "thesaurusInput",
    props: {
      numGroups: 0,  // 同义词组的数量
      synonyms: []   // 所有的同义词
    },
    data: () => ({
      inputs: [],
      addable: false,
      combinable: false
    }),
    methods: {
      sendCombineEvent() {
        this.$emit('combine')
      },
      sendAddEvent() {
        let difference = this.inputs.filter(word => this.synonyms.indexOf(word) === -1);
        if (difference.length === 0) {
          this.$emit('addGroup')
        } else {
          this.$emit('addWords')
        }
      },
      chooseColor(word) {
        return this.synonyms.indexOf(word) !== -1 ? 'primary' : 'error'
      }
    },
    watch: {
      inputs(newVal, oldVal) {
        let difference = newVal.filter(word => this.synonyms.indexOf(word) === -1);
        this.addable = difference.length > 0 && newVal.length >= 2;
        let isDelete = newVal.length < oldVal.length;
        let word;
        if (isDelete) {
          word = oldVal.filter(w => newVal.indexOf(w) === -1)[0]
        } else {
          word = newVal.filter(w => oldVal.indexOf(w) === -1)[0]
        }
        this.$emit("change", {isDelete: isDelete, word: word})
      },
      numGroups(newVal, oldVal) {
        this.combinable = newVal > 1;
      }
    }
  }
</script>

<style scoped>

</style>
