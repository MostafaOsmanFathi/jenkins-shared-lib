// Shared library step: buildApp
// Builds the Node.js application (install dependencies and syntax-check entrypoint).
//
// Usage:
//   buildApp()
//   buildApp(installCommand: 'npm ci', entrypoint: 'server.js')
def call(Map config = [:]) {
    def installCommand = config.installCommand ?: 'npm install'
    def entrypoint = config.entrypoint ?: 'server.js'

    echo "======== Build app ========"
    sh "${installCommand}"
    sh "node --check ${entrypoint}"
}
