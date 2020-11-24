def call(String label, String[] axisList, Closure execute) {
    
    def tasks = [:]
    for(int i = 0; i < axisList.size(); i++) {
        env.AXIS_NAME = axisList[i]
        tasks[env.AXIS_NAME] = {
            stage(env.AXIS_NAME) {
                node(label) {
                    execute()
                }
            }
        }
    }
    
    parallel tasks
    
}
