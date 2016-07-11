import grails.util.BuildSettings

eventStartCompile = { kind ->
    println 'Starting NPM install'
    final workdir = new File(BuildSettings.APP_BASE_DIR, 'web-app')
    final exec = new ProcessBuilder().inheritIO().directory(workdir).command("npm install").start();
    if (!exec.exitValue()) {
        println '*****************************************************'
        println '* `npm install` failed - do you have NPM installed? *'
        println '*****************************************************'
    } else {
        println 'Completed NPM install'
    }
}