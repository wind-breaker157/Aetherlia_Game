import * as THREE from "three";
import { OrbitControls } from "three/addons/controls/OrbitControls.js";


// =====================================================
// AETHERLIA - 3D WORLD MAP
// =====================================================

let scene;
let camera;
let renderer;
let controls;
let timer;

const mapObjects = [];

const MAP_POSITIONS = {
    1: { x: -6, z: -4 },
    2: { x: 0, z: -5 },
    3: { x: 6, z: -3 },
    4: { x: 6, z: 3 },
    5: { x: 0, z: 5 },
    6: { x: -6, z: 3 },
    7: { x: -10, z: 0 },
    8: { x: 10, z: 0 },
    9: { x: 0, z: 0 }
};

// =====================================================
// INIT
// =====================================================

function initWorldMap3D() {

    const container =
        document.getElementById("world-map-3d");

    if (!container) {

        console.error(
            "Không tìm thấy #world-map-3d."
        );

        return;
    }

    scene =
        new THREE.Scene();

    scene.background =
        new THREE.Color(
            0xcfe8ff
        );

    // =================================================
    // CAMERA
    // =================================================

    camera =
        new THREE.PerspectiveCamera(
            45,
            container.clientWidth /
            container.clientHeight,
            0.1,
            200
        );

    camera.position.set(
        0,
        18,
        20
    );

    // =================================================
    // RENDERER
    // =================================================

    renderer =
        new THREE.WebGLRenderer({
            antialias: true
        });

    renderer.setPixelRatio(
        Math.min(
            window.devicePixelRatio || 1,
            2
        )
    );

    renderer.setSize(
        container.clientWidth,
        container.clientHeight
    );

    renderer.shadowMap.enabled = true;

    renderer.shadowMap.type =
        THREE.PCFShadowMap;

    renderer.outputColorSpace =
        THREE.SRGBColorSpace;

    container.appendChild(
        renderer.domElement
    );

    // =================================================
    // TIMER
    // =================================================

	timer =
	new THREE.Clock();

    // =================================================
    // CONTROLS
    // =================================================

    controls =
        new OrbitControls(
            camera,
            renderer.domElement
        );

    controls.enableDamping = true;

    controls.dampingFactor =
        0.08;

    controls.enablePan = true;

    controls.minDistance = 10;
    controls.maxDistance = 35;

    controls.minPolarAngle =
        THREE.MathUtils.degToRad(30);

    controls.maxPolarAngle =
        THREE.MathUtils.degToRad(75);

    controls.target.set(
        0,
        0,
        0
    );

    // =================================================
    // LIGHT
    // =================================================

    createLights();

    // =================================================
    // SKY / WORLD
    // =================================================

    createWorld();

    // =================================================
    // ROADS
    // =================================================

    createWorldPath();

    // =================================================
    // MAP NODES
    // =================================================

    createMapNodes();

    // =================================================
    // DECORATION
    // =================================================

    createEnvironment();

    // =================================================
    // CLICK
    // =================================================

    setupRaycaster(
        container
    );

    window.addEventListener(
        "resize",
        () => resize(container)
    );

    animate();

    console.log(
        "AETHERLIA 3D World Map đã khởi tạo."
    );
}

// =====================================================
// LIGHT
// =====================================================

function createLights() {

    const ambient =
        new THREE.AmbientLight(
            0xffffff,
            2.5
        );

    scene.add(
        ambient
    );

    const sun =
        new THREE.DirectionalLight(
            0xffffff,
            4
        );

    sun.position.set(
        -10,
        18,
        10
    );

    sun.castShadow = true;

    sun.shadow.mapSize.width =
        2048;

    sun.shadow.mapSize.height =
        2048;

    scene.add(
        sun
    );

    const fill =
        new THREE.DirectionalLight(
            0xffffff,
            1.5
        );

    fill.position.set(
        10,
        8,
        -10
    );

    scene.add(
        fill
    );
}

// =====================================================
// WORLD
// =====================================================

function createWorld() {

    // Nền nước
    const waterGeometry =
        new THREE.CylinderGeometry(
            20,
            20,
            0.4,
            64
        );

    const waterMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x77bce8,
            roughness: 0.9
        });

    const water =
        new THREE.Mesh(
            waterGeometry,
            waterMaterial
        );

    water.position.y =
        -0.6;

    water.receiveShadow = true;

    scene.add(
        water
    );

    // Hòn đảo chính
    const islandGeometry =
        new THREE.CylinderGeometry(
            14,
            16,
            1.2,
            64
        );

    const islandMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x78aa65,
            roughness: 1
        });

    const island =
        new THREE.Mesh(
            islandGeometry,
            islandMaterial
        );

    island.position.y =
        0;

    island.receiveShadow = true;
    island.castShadow = true;

    scene.add(
        island
    );

    // Bờ cát
    const shoreGeometry =
        new THREE.CylinderGeometry(
            15,
            15.7,
            0.45,
            64
        );

    const shoreMaterial =
        new THREE.MeshStandardMaterial({
            color: 0xd8bf85,
            roughness: 1
        });

    const shore =
        new THREE.Mesh(
            shoreGeometry,
            shoreMaterial
        );

    shore.position.y =
        0.55;

    scene.add(
        shore
    );

    // Cỏ phủ phía trên
    const grassGeometry =
        new THREE.CylinderGeometry(
            14.3,
            14.8,
            0.25,
            64
        );

    const grassMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x6ca657,
            roughness: 1
        });

    const grass =
        new THREE.Mesh(
            grassGeometry,
            grassMaterial
        );

    grass.position.y =
        0.82;

    grass.receiveShadow = true;

    scene.add(
        grass
    );
}

// =====================================================
// WORLD PATH
// =====================================================

function createWorldPath() {

    const points = [
        new THREE.Vector3(-6, 1, -4),
        new THREE.Vector3(0, 1, -5),
        new THREE.Vector3(6, 1, -3),
        new THREE.Vector3(6, 1, 3),
        new THREE.Vector3(0, 1, 5),
        new THREE.Vector3(-6, 1, 3),
        new THREE.Vector3(-10, 1, 0),
        new THREE.Vector3(0, 1, 0),
        new THREE.Vector3(10, 1, 0)
    ];

    for (let i = 0; i < points.length - 1; i++) {

        createPathSegment(
            points[i],
            points[i + 1]
        );
    }
}

function createPathSegment(
    start,
    end
) {

    const direction =
        new THREE.Vector3()
            .subVectors(
                end,
                start
            );

    const length =
        direction.length();

    const geometry =
        new THREE.BoxGeometry(
            1.8,
            0.12,
            length
        );

    const material =
        new THREE.MeshStandardMaterial({
            color: 0xcaa96b,
            roughness: 1
        });

    const path =
        new THREE.Mesh(
            geometry,
            material
        );

    const midpoint =
        new THREE.Vector3()
            .addVectors(
                start,
                end
            )
            .multiplyScalar(
                0.5
            );

    path.position.copy(
        midpoint
    );

    path.position.y =
        0.95;

    path.rotation.y =
        -Math.atan2(
            direction.x,
            direction.z
        );

    path.receiveShadow = true;

    scene.add(
        path
    );
}

// =====================================================
// MAP NODES
// =====================================================

function createMapNodes() {

    const nodes =
        document.querySelectorAll(
            "[data-map-number]"
        );

    nodes.forEach(
        node => {

            const mapNumber =
                Number(
                    node.dataset.mapNumber
                );

            const name =
                node.dataset.mapName ||
                `Map ${mapNumber}`;

            const terrain =
                node.dataset.mapTerrain ||
                "";

            const unlocked =
                node.dataset.unlocked ===
                "true";

            const position =
                MAP_POSITIONS[
                    mapNumber
                ] ||
                {
                    x: 0,
                    z: 0
                };

            createMapNode({
                mapNumber,
                name,
                terrain,
                unlocked,
                x: position.x,
                z: position.z
            });
        }
    );
}

// =====================================================
// CREATE SINGLE MAP NODE
// =====================================================

function createMapNode(data) {

    const group =
        new THREE.Group();

    group.userData.mapNumber =
        data.mapNumber;

    group.userData.unlocked =
        data.unlocked;

    // =================================================
    // PLATFORM
    // =================================================

    const platformGeometry =
        new THREE.CylinderGeometry(
            1.35,
            1.5,
            0.35,
            32
        );

    const platformMaterial =
        new THREE.MeshStandardMaterial({
            color:
                data.unlocked
                    ? 0xf4d35e
                    : 0x777777,
            roughness: 0.8
        });

    const platform =
        new THREE.Mesh(
            platformGeometry,
            platformMaterial
        );

    platform.position.y =
        1.2;

    platform.castShadow = true;
    platform.receiveShadow = true;

    group.add(
        platform
    );

    // =================================================
    // CENTER CRYSTAL
    // =================================================

    const crystalGeometry =
        new THREE.OctahedronGeometry(
            0.65,
            1
        );

    const crystalMaterial =
        new THREE.MeshStandardMaterial({
            color:
                data.unlocked
                    ? 0x3b82f6
                    : 0x555555,

            emissive:
                data.unlocked
                    ? 0x164e63
                    : 0x000000,

            emissiveIntensity:
                data.unlocked
                    ? 0.8
                    : 0
        });

    const crystal =
        new THREE.Mesh(
            crystalGeometry,
            crystalMaterial
        );

    crystal.position.y =
        2;

    crystal.castShadow = true;

    group.add(
        crystal
    );

    // =================================================
    // RING
    // =================================================

    const ringGeometry =
        new THREE.TorusGeometry(
            1.45,
            0.05,
            12,
            48
        );

    const ringMaterial =
        new THREE.MeshBasicMaterial({
            color:
                data.unlocked
                    ? 0x60a5fa
                    : 0x555555
        });

    const ring =
        new THREE.Mesh(
            ringGeometry,
            ringMaterial
        );

    ring.rotation.x =
        Math.PI / 2;

    ring.position.y =
        1.35;

    group.add(
        ring
    );

    // =================================================
    // NAME LABEL
    // =================================================

    const label =
        createLabel(
            `MAP ${data.mapNumber}`,
            data.unlocked
        );

    label.position.set(
        0,
        3,
        0
    );

    group.add(
        label
    );

    group.position.set(
        data.x,
        0,
        data.z
    );

    scene.add(
        group
    );

    mapObjects.push(
        group
    );
}

// =====================================================
// LABEL
// =====================================================

function createLabel(
    text,
    unlocked
) {

    const canvas =
        document.createElement(
            "canvas"
        );

    canvas.width = 256;
    canvas.height = 64;

    const context =
        canvas.getContext(
            "2d"
        );

    context.clearRect(
        0,
        0,
        canvas.width,
        canvas.height
    );

    context.fillStyle =
        unlocked
            ? "rgba(15,23,42,.85)"
            : "rgba(55,65,81,.85)";

    context.roundRect(
        8,
        8,
        240,
        48,
        16
    );

    context.fill();

    context.fillStyle =
        "#ffffff";

    context.font =
        "bold 24px Arial";

    context.textAlign =
        "center";

    context.textBaseline =
        "middle";

    context.fillText(
        text,
        128,
        32
    );

    const texture =
        new THREE.CanvasTexture(
            canvas
        );

    texture.colorSpace =
        THREE.SRGBColorSpace;

    const material =
        new THREE.SpriteMaterial({
            map: texture,
            transparent: true
        });

    return new THREE.Sprite(
        material
    );
}

// =====================================================
// ENVIRONMENT
// =====================================================

function createEnvironment() {

    // Cây
    const treePositions = [
        [-11, -6],
        [-8, -7],
        [-3, -8],
        [4, -7],
        [10, -5],
        [11, 6],
        [7, 8],
        [2, 8],
        [-5, 7],
        [-11, 5]
    ];

    treePositions.forEach(
        ([x, z]) => {
            createTree(
                x,
                z
            );
        }
    );

    // Đá
    const rockPositions = [
        [-4, -2],
        [4, 1],
        [-8, 2],
        [8, -1],
        [1, -1]
    ];

    rockPositions.forEach(
        ([x, z]) => {
            createRock(
                x,
                z
            );
        }
    );
}

// =====================================================
// TREE
// =====================================================

function createTree(
    x,
    z
) {

    const group =
        new THREE.Group();

    const trunk =
        new THREE.Mesh(
            new THREE.CylinderGeometry(
                0.16,
                0.22,
                1.5,
                8
            ),
            new THREE.MeshStandardMaterial({
                color: 0x795548
            })
        );

    trunk.position.y =
        1.65;

    trunk.castShadow = true;

    group.add(
        trunk
    );

    const leaves =
        new THREE.Mesh(
            new THREE.SphereGeometry(
                0.85,
                12,
                10
            ),
            new THREE.MeshStandardMaterial({
                color: 0x3f8f4c,
                roughness: 1
            })
        );

    leaves.position.y =
        2.7;

    leaves.castShadow = true;

    group.add(
        leaves
    );

    group.position.set(
        x,
        0.8,
        z
    );

    scene.add(
        group
    );
}

// =====================================================
// ROCK
// =====================================================

function createRock(
    x,
    z
) {

    const rock =
        new THREE.Mesh(
            new THREE.DodecahedronGeometry(
                0.55,
                0
            ),
            new THREE.MeshStandardMaterial({
                color: 0x87909a,
                roughness: 1
            })
        );

    rock.position.set(
        x,
        1.25,
        z
    );

    rock.scale.set(
        1.3,
        0.8,
        0.9
    );

    rock.rotation.y =
        Math.random() *
        Math.PI;

    rock.castShadow = true;

    scene.add(
        rock
    );
}

// =====================================================
// RAYCASTER
// =====================================================

function setupRaycaster(
    container
) {

    const raycaster =
        new THREE.Raycaster();

    const mouse =
        new THREE.Vector2();

    renderer.domElement.addEventListener(
        "click",
        function(event) {

            const rect =
                renderer.domElement.getBoundingClientRect();

            mouse.x =
                (
                    (event.clientX - rect.left) /
                    rect.width
                ) * 2 - 1;

            mouse.y =
                -(
                    (event.clientY - rect.top) /
                    rect.height
                ) * 2 + 1;

            raycaster.setFromCamera(
                mouse,
                camera
            );

            const intersects =
                raycaster.intersectObjects(
                    mapObjects,
                    true
                );

            if (!intersects.length) {
                return;
            }

            let object =
                intersects[0].object;

            while (
                object &&
                !object.userData.mapNumber
            ) {

                object =
                    object.parent;
            }

            if (
                !object ||
                !object.userData.mapNumber
            ) {
                return;
            }

            const mapNumber =
                object.userData.mapNumber;

            const unlocked =
                object.userData.unlocked;

            if (!unlocked) {

                showLockedMessage();

                return;
            }

            window.location.href =
                `/map/${mapNumber}`;
        }
    );
}

// =====================================================
// LOCKED MESSAGE
// =====================================================

function showLockedMessage() {

    const message =
        document.getElementById(
            "map3d-message"
        );

    if (!message) {
        return;
    }

    message.textContent =
        "Khu vực này chưa được mở khóa.";

    message.classList.add(
        "show"
    );

    clearTimeout(
        message._timer
    );

    message._timer =
        setTimeout(
            () => {
                message.classList.remove(
                    "show"
                );
            },
            1800
        );
}

// =====================================================
// RESIZE
// =====================================================

function resize(
    container
) {

    const width =
        container.clientWidth;

    const height =
        container.clientHeight;

    if (
        width <= 0 ||
        height <= 0
    ) {
        return;
    }

    camera.aspect =
        width / height;

    camera.updateProjectionMatrix();

    renderer.setSize(
        width,
        height
    );
}

// =====================================================
// ANIMATE
// =====================================================

function animate() {

    requestAnimationFrame(
        animate
    );

    const delta =
        timer.getDelta();

    const elapsed =
        timer.elapsedTime;

    controls.update();

    mapObjects.forEach(
        object => {

            const crystal =
                object.children.find(
                    child =>
                        child.geometry &&
                        child.geometry.type ===
                        "OctahedronGeometry"
                );

            if (crystal) {

                crystal.rotation.y +=
                    delta * 1.2;

                crystal.position.y =
                    2 +
                    Math.sin(
                        elapsed * 2
                    ) * 0.12;
            }
        }
    );

    renderer.render(
        scene,
        camera
    );
}

// =====================================================
// START
// =====================================================

if (
    document.readyState ===
    "loading"
) {

    document.addEventListener(
        "DOMContentLoaded",
        initWorldMap3D
    );

} else {

    initWorldMap3D();
}