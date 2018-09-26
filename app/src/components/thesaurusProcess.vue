<template>
  <div>
    <v-layout wrap justify-center>
      <v-flex xs10>
        <v-switch label="允许删除" v-model="editable"></v-switch>
      </v-flex>
      <v-flex xs10>
        <v-layout justify-start align-start row wrap>
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
              <transition  name="fade" slot="activator">
                <v-btn v-if="!combinable && addable" color="green" round dark medium
                       @click="addWords"
                >
                  <v-icon>add</v-icon>
                </v-btn>
                <v-btn v-if="combinable" color="green" round dark medium
                       @click="combineGroups">
                  <v-icon>add_circle_outline</v-icon>
                </v-btn>
              </transition>
              <span>{{!combinable ? '添加同义词' : '合并同义词组'}}</span>
            </v-tooltip>
          </v-flex>
        </v-layout>
      </v-flex>
      <v-flex xs10 class="py-2">
        <v-flex xs10 class="py-2" v-if="synonyms.length === 0">
          <v-alert :value="true" type="info">
            请输入词语以搜索同义词。
          </v-alert>
        </v-flex>
        <transition-group name="list">
          <v-flex xs12 class="py-1" v-for="(group, groupId) in synonymsGroups" :key="groupId">
            <thesaurus-group
              :editable="editable"
              :groupId="groupId"
              :words="group"
              :inputs="inputs"
              @clear="deleteSynonymsGroup"
              @remove="removeWord"
            />
          </v-flex>
        </transition-group>
      </v-flex>
    </v-layout>
  </div>
</template>

<script>
  import {THESAURUS} from "../configs/srapp.api";
  import ThesaurusGroup from "./thesaurusGroup";

  export default {
    name: "thesaurusProcess",
    components: {ThesaurusGroup},
    data: () => ({
      addable: false,
      editable: false,
      inputs: [],
      belong: {},
      groupCount: {},
      synonymsGroups: {},
    }),
    methods: {
      chooseColor(word) {
        return this.synonyms.indexOf(word) !== -1 ? 'primary' : 'error';
      },
      clear() {
        this.synonymsGroups = {};
        this.groupCount = {};
        this.belong = {};
      },
      /**
       * 添加单词到同义词组中
       */
      addWords() {
        // this.inputs ∩ this.synonyms
        let intersection = this.inputs.filter(x => this.synonyms.indexOf(x) !== -1);
        // this.inputs - this.synonyms
        let difference = this.inputs.filter(x => this.synonyms.indexOf(x) === -1);

        let addStat;
        let groupId = Object.keys(this.synonymsGroups)[0];
        let isAddGroup = false;

        if (intersection.length === 0) {
          isAddGroup = true;
          addStat = THESAURUS + "?words=" + this.inputs.join(',')
        } else {
          isAddGroup = false;
          addStat = THESAURUS + "?words=" + this.inputs.join(',') + '&groupId=' + groupId
        }

        this.$http.post(addStat)
          .then(response => {
            response.json().then(result => {
              if (result['numFound'] === 1) {
                let synonymsGroup = result['synonymsGroup'][0];
                this.$message.success("'" + difference.join(',') + "'已加入到同义词组!'");

                if (isAddGroup) {
                  let groupId = synonymsGroup['groupId'];
                  this.$set(this.synonymsGroups, groupId, synonymsGroup['synonyms']);
                  this.$set(this.groupCount, groupId, this.inputs.length)
                } else {
                  let words = this.synonymsGroups[groupId];
                  this.$set(this.synonymsGroups, groupId, words.concat(difference));
                  this.$set(this.groupCount, groupId, this.inputs.length)
                }
              }
            }, errors => console.log(errors))
          });

      },
      combineGroups() {
        let groupIds = Object.keys(this.synonymsGroups);
        this.$http.post(THESAURUS + "/combine", {
          groupIds: groupIds
        }).then(response => {
          response.json().then(result => {
            if (result['combination'].length === 1) {
              let combination = result['combination'][0];
              let groupId = combination['groupId'];
              let synonyms = combination['synonyms'];

              // 组合完成后，更新数据状态
              this.$set(this.synonymsGroups, groupId, synonyms);
              groupIds.forEach(id => this.$delete(this.synonymsGroups, id));

              let intersection = synonyms.filter(word => this.inputs.indexOf(word) !== -1);
              intersection.forEach(word => this.$set(this.belong, word, groupId));
              this.$set(this.groupCount, intersection.length);
            }
          }, errors => {
            this.$message.error("发生未知错误，请检查请求参数！")
          })
        })
      },
      removeWord(data) {
        this.$http.delete(THESAURUS + "?word=" + data.word + "&groupId=" + data.groupId)
          .then(response => {
            if (response.status === 204) {
              this.$message.success("单词：'" + data.word + "'成功从同义词组中删除！");
              let words = this.synonymsGroups[data.groupId];
              words.splice(words.indexOf(data.word), 1);
              this.$set(this.synonymsGroups, data.groupId, [...words]);
            }
          })
      },
      deleteSynonymsGroup(groupId) {
        this.$confirm("此操作将永久删除该组同义词且不可逆，你确定吗？", '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http.delete(THESAURUS + '?groupId=' + groupId)
            .then(response => {
              if (response.status === 204) {
                this.$message({
                  type: 'success',
                  message: '同义词组已删除'
                });
                this.$delete(this.synonymsGroups, groupId)
              }
            }, errors => this.$notify.error("请检查请求参数！"))
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消'
          })
        });
      },
      getSynonymGroup(word) {
        this.$http.get(THESAURUS + "/" + word)
          .then(response => {
            if (!!response.body) {
              response.json().then(result => {
                if (result['numFound'] === 1) {
                  let synonymsGroup = result['synonymsGroup'][0];
                  let groupId = synonymsGroup['groupId'];
                  let synonyms = synonymsGroup['synonyms']
                    .sort((a, b) => a.localeCompare(b, 'zh-Hans-CN', {sensitivity: 'accent'}));
                  this.$set(this.belong, word, groupId);
                  this.$set(this.synonymsGroups, groupId, synonyms);
                  this.$set(this.groupCount, groupId, this.groupCount[groupId] == null ? 1 : this.groupCount[groupId] + 1);
                } else {
                  // 更新可添加状态
                  let difference = this.inputs.filter(word => this.synonyms.indexOf(word) === -1);
                  this.addable = difference.length > 0 && this.inputs.length >= 2;
                }
              });
            }
          }, errors => {
            if (errors.status === 400) {
              this.$notify.error("请检查请求参数！")
            }
          })
      },
    },
    computed: {
      synonyms() {
        return Object.values(this.synonymsGroups).reduce((a, b) => [...a, ...b], [])
      },
      combinable() {
        return Object.keys(this.synonymsGroups).length > 1;
      }
    },
    watch: {
      inputs(newVal, oldVal) {
        if (newVal.length === 0) {
          this.clear();
        }
        if (newVal.length < 2){
          this.addable = false
        }
        let isDelete = newVal.length < oldVal.length;
        let word;
        // 当移除一个单词时
        if (isDelete) {
          word = oldVal.filter(w => newVal.indexOf(w) === -1)[0];
          let groupId = this.belong[word];
          if (!!groupId) {
            this.$set(this.groupCount, groupId, this.groupCount[groupId] - 1);
            // 当删除该组最后一个单词时，将同义词组id移除
            if (this.groupCount[groupId] === 0) {
              this.$delete(this.synonymsGroups, groupId);
              this.$delete(this.groupCount, groupId)
            }
          }
        } else {
          word = newVal.filter(w => oldVal.indexOf(w) === -1)[0];
          this.getSynonymGroup(word)
        }
      }
    }
  }
</script>

<style scoped>
  .fade-enter-active, .fade-leave-active {
    transition: all .5s;
  }

  .fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */
  {
    opacity: 0;
    transform: scaleY(0);
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
    transform: translateY(30px);
  }
</style>
