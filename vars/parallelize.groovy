def call(String label, String[] axisList, Closure execute) {
    
    def tasks = [:]
    for(int i = 0; i < axisList.size(); i++) {
        def axisName = axisList[i]
        tasks[axisName] = {
            node(label) {
                withEnv(env + [AXIS_NAME=axisName]) {
                    stage(axisName) {
                        execute()
                    }
                }
            }
        }
    }
    
    parallel tasks
    
}
