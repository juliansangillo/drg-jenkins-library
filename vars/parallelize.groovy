def call(String labelPrefix, String[] axisValues, Closure execute) {
    
    def tasks = [:]
    for(int i = 0; i < axisValues.size(); i++) {
        def axisValue = axisValues[i]
        def label = labelPrefix + '-' + i
        tasks[axisValue] = {
            stage(name) {
                node(label) {
                    execute()
                }
            }
        }
    }
    
    parallel tasks
    
}
