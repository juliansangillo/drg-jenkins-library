def call(String labelPrefix, String[] axisValues, Closure execute) {
    
    def tasks = [:]
    for(int i = 0; i < axis.size(); i++) {
        def axisValue = axis[i]
        def label = prefix + '-' + i
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
