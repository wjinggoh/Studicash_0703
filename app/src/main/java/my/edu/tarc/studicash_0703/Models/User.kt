package my.edu.tarc.studicash_0703.Models

class User {
    var uid: String? = null
    var image:String?=null
    var name:String?=null
    var email:String?=null
    var fcmToken: String? = null
    var password:String?=null
    var gender:String?=null

    constructor()

    constructor(uid: String?, image: String?, name: String?, email: String?, password: String?, gender: String?, fcmToken: String?) {
        this.uid = uid
        this.image = image
        this.name = name
        this.email = email
        this.password = password
        this.gender = gender
        this.fcmToken = fcmToken
    }

    constructor(uid:String?,image: String?, name: String?, email: String?, password: String?,gender:String?) {
        this.uid=uid
        this.image = image
        this.name = name
        this.email = email
        this.password = password
        this.gender=gender
    }

    constructor(name: String?, email: String?, password: String?,gender: String?) {
        this.image = image
        this.name = name
        this.email = email
        this.password = password
        this.gender=gender
    }

    constructor(image: String?,name: String?) {
        this.image = image
        this.name = name
    }


}