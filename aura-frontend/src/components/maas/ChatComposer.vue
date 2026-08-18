<script setup lang="ts">
import { ref } from 'vue'
const props = defineProps<{ busy?: boolean; placeholder?: string }>()
const emit = defineEmits<{ send: [text: string]; stop: []; retry: [] }>()
const text = ref('')
function submit(){ if(text.value.trim()){ emit('send',text.value.trim()); text.value='' } }
</script>
<template>
  <form class="composer" @submit.prevent="submit">
    <button class="attach" type="button" aria-label="添加附件">＋</button>
    <textarea v-model="text" :placeholder="placeholder || '问问 Aura 任何事情…'" :disabled="busy" aria-label="输入消息" @keydown.enter.exact.prevent="submit" />
    <div class="composer-footer"><button class="model" type="button" aria-label="选择模型">Aura · 标准 <span aria-hidden="true">⌄</span></button><button v-if="busy" class="submit stop" type="button" @click="emit('stop')"><span aria-hidden="true">■</span> 停止</button><button v-else class="submit" type="submit" :disabled="!text.trim()"><span aria-hidden="true">↑</span></button></div>
  </form>
</template>
<style scoped>
.composer{position:relative;display:flex;align-items:flex-end;gap:9px;margin:0 auto 18px;padding:12px 13px;border:1px solid rgba(132,108,196,.2);border-radius:18px;background:rgba(255,255,255,.9);box-shadow:0 14px 34px rgba(62,43,114,.12);max-width:min(820px,calc(100% - 28px))}.composer textarea{flex:1;min-height:44px;max-height:140px;padding:10px 3px;border:0;outline:0;resize:none;background:transparent;color:#312747;font:inherit;font-size:14px;line-height:1.5}.composer textarea::placeholder{color:#a39bb5}.attach{display:grid;place-items:center;width:32px;height:32px;margin-bottom:5px;border:0;border-radius:10px;background:#f2edff;color:#7657c3;font-size:21px;line-height:1;cursor:pointer}.composer-footer{display:flex;align-items:center;gap:8px;margin-bottom:4px}.model{border:0;background:transparent;color:#8b829f;font-size:11px;white-space:nowrap;cursor:pointer}.model span{font-size:14px}.submit{display:grid;place-items:center;width:34px;height:34px;border:0;border-radius:11px;background:linear-gradient(135deg,#8061dd,#6045a9);color:#fff;font-size:18px;cursor:pointer;box-shadow:0 5px 12px rgba(96,69,169,.22)}.submit:disabled{opacity:.35;box-shadow:none;cursor:not-allowed}.submit.stop{width:auto;padding:0 11px;font-size:11px;background:#f1edf8;color:#66557d;box-shadow:none}.submit.stop span{font-size:9px;margin-right:3px}@media (max-width:640px){.composer{margin-bottom:10px;padding:9px 10px;border-radius:15px}.model{display:none}.composer textarea{font-size:13px}.attach{width:29px;height:29px}}
</style>
