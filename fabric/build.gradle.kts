plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra
val modmenu_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    modCompileOnly("com.terraformersmc:modmenu:${modmenu_version}") {
        exclude(module = "fabric-api")
    }
}
