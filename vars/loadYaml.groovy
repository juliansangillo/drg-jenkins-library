import org.yaml.snakeyaml.Yaml

def call(String file) {
    sh "ls ${file}"
    return new Yaml().load((file as File).text)
}
