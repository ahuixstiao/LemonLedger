export async function loadFactoryOptions(state, queryFactoryListApi) {
  const { data: res } = await queryFactoryListApi()
  state.factoryList = res?.data || []
}

export async function loadFactoryCategoryOptions(state, queryFactoryCategoryApi) {
  const { data: res } = await queryFactoryCategoryApi()
  state.categoryList = res?.data || []
}

export function resetReactiveForm(formModel, initialModel, formRef) {
  Object.assign(formModel, initialModel)
  formRef?.value?.clearValidate?.()
}

export function openCreateDialog(formModel, initialModel, state, modeKey, visibleKey) {
  Object.assign(formModel, initialModel)
  state[modeKey] = 0
  state[visibleKey] = true
}

export function openEditDialog(formModel, source, state, modeKey, visibleKey) {
  Object.assign(formModel, source)
  state[modeKey] = 1
  state[visibleKey] = true
}

export async function validateDialogForm(formRef, mode, onCreate, onUpdate, onInvalid) {
  if (!formRef) return
  await formRef.validate(valid => {
    if (valid) {
      if (mode === 0) onCreate()
      else if (mode === 1) onUpdate()
    } else {
      onInvalid?.()
    }
  })
}
