import com.deltarenegadegames.unityci.Matrix

def call(String labelPrefix, String[] axisValues, Closure execute) {
    
    def stageBody = { name, label ->
        stage(name) {
            node(label) {
                execute()
            }
        }
    }
    
    def tasks = new Matrix(prefix: labelPrefix, axis: axisValues, body: stageBody).getTasks()
    
    parallel tasks
    
}
