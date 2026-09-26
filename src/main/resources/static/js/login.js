import * as THREE from "three";

import {
    GLTFLoader
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/loaders/GLTFLoader.js";

import {
    OrbitControls
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/controls/OrbitControls.js";


/* =========================================================
   AETHERLIA - LOGIN 3D
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    () => {


        /* =====================================================
           PASSWORD TOGGLE
        ====================================================== */

        const password =
            document.getElementById(
                "password"
            );


        const toggle =
            document.getElementById(
                "togglePassword"
            );


        if (
            password
            &&
            toggle
        ) {

            toggle.addEventListener(
                "click",
                () => {

                    if (
                        password.type ===
                        "password"
                    ) {

                        password.type =
                            "text";

                        toggle.textContent =
                            "🙈";

                    } else {

                        password.type =
                            "password";

                        toggle.textContent =
                            "👁";

                    }

                }
            );

        }



        /* =====================================================
           3D ELEMENTS
        ====================================================== */

        const container =
            document.getElementById(
                "loginMonster3d"
            );


        const nameElement =
            document.getElementById(
                "loginMonsterName"
            );


        const typeElement =
            document.getElementById(
                "loginMonsterType"
            );


        if (
            !container
            ||
            !nameElement
            ||
            !typeElement
        ) {

            console.warn(
                "Login 3D viewer không được khởi tạo."
            );

            return;

        }



        /* =====================================================
           MONSTERS
        ====================================================== */

        const monsters = [

            {
                name:
                    "Spriglet",

                type:
                    "NATURE",

                url:
                    "/models/monsters/spriglet/spriglet.glb"
            },


            {
                name:
                    "Pyron",

                type:
                    "FIRE",

                url:
                    "/models/monsters/pyron/pyron.glb"
            },


            {
                name:
                    "Aquaff",

                type:
                    "WATER",

                url:
                    "/models/monsters/aquaff/aquaff.glb"
            }

        ];



        /* =====================================================
           SCENE
        ====================================================== */

        const scene =
            new THREE.Scene();


        /* =====================================================
           CAMERA
        ====================================================== */

        const camera =
            new THREE.PerspectiveCamera(
                34,
                1,
                0.1,
                100
            );


        camera.position.set(
            0,
            1.45,
            5
        );



        /* =====================================================
           RENDERER
        ====================================================== */

        const renderer =
            new THREE.WebGLRenderer({
                antialias:
                    true,

                alpha:
                    true
            });


        renderer.setPixelRatio(
            Math.min(
                window.devicePixelRatio || 1,
                2
            )
        );


        renderer.outputColorSpace =
            THREE.SRGBColorSpace;


        renderer.shadowMap.enabled =
            true;


        renderer.shadowMap.type =
            THREE.PCFShadowMap;


        container.appendChild(
            renderer.domElement
        );



        /* =====================================================
           LIGHT
        ====================================================== */

        const hemisphere =
            new THREE.HemisphereLight(
                0xffffff,
                0x64748b,
                2.5
            );


        scene.add(
            hemisphere
        );


        const keyLight =
            new THREE.DirectionalLight(
                0xffffff,
                3.5
            );


        keyLight.position.set(
            -4,
            6,
            5
        );


        keyLight.castShadow =
            true;


        scene.add(
            keyLight
        );


        const fillLight =
            new THREE.DirectionalLight(
                0xa78bfa,
                1.8
            );


        fillLight.position.set(
            4,
            3,
            -4
        );


        scene.add(
            fillLight
        );



        /* =====================================================
           SHADOW
        ====================================================== */

        const shadow =
            new THREE.Mesh(
                new THREE.CircleGeometry(
                    1.2,
                    32
                ),
                new THREE.MeshBasicMaterial({
                    color:
                        0x000000,

                    transparent:
                        true,

                    opacity:
                        0.12
                })
            );


        shadow.rotation.x =
            -Math.PI / 2;


        shadow.position.y =
            0.01;


        scene.add(
            shadow
        );



        /* =====================================================
           ORBIT CONTROLS
        ====================================================== */

        const controls =
            new OrbitControls(
                camera,
                renderer.domElement
            );


        controls.enableDamping =
            true;


        controls.dampingFactor =
            0.06;


        controls.enablePan =
            false;


        controls.enableZoom =
            true;


        controls.minDistance =
            2.7;


        controls.maxDistance =
            6.5;


        controls.target.set(
            0,
            1.05,
            0
        );


        controls.autoRotate =
            true;


        controls.autoRotateSpeed =
            0.8;



        /* =====================================================
           LOADER
        ====================================================== */

        const loader =
            new GLTFLoader();


        let currentModel =
            null;


        let mixer =
            null;


        let currentIndex =
            0;



        /* =====================================================
           CLEAR MODEL
        ====================================================== */

        function clearModel() {

            if (
                !currentModel
            ) {
                return;
            }


            scene.remove(
                currentModel
            );


            currentModel.traverse(
                (
                    object
                ) => {

                    if (
                        object.geometry
                    ) {

                        object.geometry
                            .dispose();

                    }


                    if (
                        object.material
                    ) {

                        if (
                            Array.isArray(
                                object.material
                            )
                        ) {

                            object.material.forEach(
                                (
                                    material
                                ) => {

                                    material.dispose();

                                }
                            );

                        } else {

                            object.material
                                .dispose();

                        }

                    }

                }
            );


            currentModel =
                null;


            mixer =
                null;

        }



        /* =====================================================
           LOAD MONSTER
        ====================================================== */

        function loadMonster(
            index
        ) {

            const monster =
                monsters[index];


            nameElement.textContent =
                monster.name;


            typeElement.textContent =
                monster.type;


            loader.load(

                monster.url,

                (gltf) => {

                    clearModel();


                    const model =
                        gltf.scene;


                    currentModel =
                        model;


                    /* =========================================
                       BOUNDING BOX
                    ========================================== */

                    const box =
                        new THREE.Box3()
                            .setFromObject(
                                model
                            );


                    const size =
                        new THREE.Vector3();


                    box.getSize(
                        size
                    );


                    const maxSize =
                        Math.max(
                            size.x,
                            size.y,
                            size.z
                        );


                    /* =========================================
                       NORMALIZE
                    ========================================== */

                    if (
                        maxSize > 0
                    ) {

                        const scale =
                            3.0 /
                            maxSize;


                        model.scale.setScalar(
                            scale
                        );

                    }


                    /* =========================================
                       RECENTER
                    ========================================== */

                    const newBox =
                        new THREE.Box3()
                            .setFromObject(
                                model
                            );


                    const center =
                        new THREE.Vector3();


                    newBox.getCenter(
                        center
                    );


                    model.position.x =
                        -center.x;


                    model.position.z =
                        -center.z;


                    model.position.y =
                        -newBox.min.y;


                    /* =========================================
                       SHADOW
                    ========================================== */

                    const newSize =
                        new THREE.Vector3();


                    newBox.getSize(
                        newSize
                    );


                    const shadowSize =
                        Math.max(
                            newSize.x,
                            newSize.z
                        );


                    shadow.scale.set(
                        shadowSize * 0.85,
                        shadowSize * 0.5,
                        1
                    );


                    /* =========================================
                       MESH
                    ========================================== */

                    model.traverse(
                        (
                            object
                        ) => {

                            if (
                                object.isMesh
                            ) {

                                object.castShadow =
                                    true;


                                object.receiveShadow =
                                    true;

                            }

                        }
                    );


                    /* =========================================
                       ANIMATION
                    ========================================== */

                    if (
                        gltf.animations
                        &&
                        gltf.animations.length
                    ) {

                        mixer =
                            new THREE.AnimationMixer(
                                model
                            );


                        let idle =
                            gltf.animations.find(
                                (
                                    animation
                                ) =>
                                    animation.name
                                        .toLowerCase()
                                        .includes(
                                            "idle"
                                        )
                            );


                        if (
                            !idle
                        ) {

                            idle =
                                gltf.animations[0];

                        }


                        if (
                            idle
                        ) {

                            mixer
                                .clipAction(
                                    idle
                                )
                                .play();

                        }

                    }


                    scene.add(
                        model
                    );


                },

                undefined,

                (error) => {

                    console.error(
                        "Không thể tải Monster 3D:",
                        monster.name,
                        error
                    );

                }

            );

        }



        /* =====================================================
           CHANGE MONSTER
        ====================================================== */

        function changeMonster() {

            currentIndex =
                (
                    currentIndex + 1
                )
                %
                monsters.length;


            loadMonster(
                currentIndex
            );

        }



        /* =====================================================
           RESIZE
        ====================================================== */

        function resize() {

            const width =
                container.clientWidth;


            const height =
                container.clientHeight;


            if (
                width <= 0
                ||
                height <= 0
            ) {

                return;
            }


            renderer.setSize(
                width,
                height,
                false
            );


            camera.aspect =
                width /
                height;


            camera.updateProjectionMatrix();

        }


        resize();


        const resizeObserver =
            new ResizeObserver(
                resize
            );


        resizeObserver.observe(
            container
        );



        /* =====================================================
           START
        ====================================================== */

        loadMonster(
            currentIndex
        );


        setInterval(
            changeMonster,
            7000
        );



        /* =====================================================
           ANIMATION
        ====================================================== */

        const clock =
            new THREE.Clock();


        function animate() {

            requestAnimationFrame(
                animate
            );


            const delta =
                clock.getDelta();


            if (
                mixer
            ) {

                mixer.update(
                    delta
                );

            }


            controls.update();


            renderer.render(
                scene,
                camera
            );

        }


        animate();


        /* =====================================================
           DEBUG
        ====================================================== */

        window.AetherliaLogin3D = {

            scene,
            camera,
            renderer,

            monsters,

            changeMonster,

            loadMonster

        };


        console.log(
            "Aetherlia Login 3D đã khởi tạo."
        );

    }
);