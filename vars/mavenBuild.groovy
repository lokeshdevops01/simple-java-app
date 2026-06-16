def call () {
  stage ('Build using shared library'){
    echo "running maven build from shared library..."
    sh 'mvn clean package'
  }
}
