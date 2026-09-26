const player =
    document.getElementById("player");

const playerX =
    document.getElementById("playerX");

const playerY =
    document.getElementById("playerY");

const locationCodeElement =
    document.getElementById("locationCode");

const explorePanel =
    document.getElementById("explorePanel");

const gameWorld =
    document.getElementById("gameWorld");


const csrfTokenElement =
    document.querySelector(
        'meta[name="_csrf"]'
    );

const csrfHeaderElement =
    document.querySelector(
        'meta[name="_csrf_header"]'
    );


const csrfToken =
    csrfTokenElement
        ? csrfTokenElement.content
        : null;

const csrfHeader =
    csrfHeaderElement
        ? csrfHeaderElement.content
        : null;


const pressedKeys =
    new Set();

const pressedButtons =
    new Set();


let movementRequestRunning =
    false;

let encounterChecking =
    false;


/* =========================================================
   ELEMENT CHECK
========================================================= */

if (!player) {
    console.error(
        "Không tìm thấy #player"
    );
}

if (!playerX) {
    console.error(
        "Không tìm thấy #playerX"
    );
}

if (!playerY) {
    console.error(
        "Không tìm thấy #playerY"
    );


}


/* =========================================================
   UPDATE LOCATION
========================================================= */

function updateLocation(
    newLocationCode
) {

    if (!newLocationCode) {
        return;
    }


    if (locationCodeElement) {

        locationCodeElement.textContent =
            newLocationCode;
    }


    if (explorePanel) {

        if (
            newLocationCode ===
            "route_grass"
        ) {

            explorePanel.style.display =
                "block";

        } else {

            explorePanel.style.display =
                "none";
        }
    }
}


/* =========================================================
   CHECK RANDOM ENCOUNTER
========================================================= */

function checkRandomEncounter() {

    if (encounterChecking) {
        return;
    }


    if (!csrfToken || !csrfHeader) {

        console.error(
            "Không tìm thấy CSRF token."
        );

        return;
    }


    if (!locationCodeElement) {
        return;
    }


    const location =
        locationCodeElement.textContent
            .trim();


    if (
        location !==
        "route_grass"
    ) {
        return;
    }


    encounterChecking =
        true;


    fetch(
        "/encounter/check",
        {
            method: "POST",

            headers: {

                [csrfHeader]:
                    csrfToken,

                "Content-Type":
                    "application/x-www-form-urlencoded"
            }
        }
    )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Encounter check failed. HTTP " +
                    response.status
                );
            }


            return response.json();
        })


        .then(function(data) {

            console.log(
                "Encounter check:",
                data
            );


            /*
             * Gặp Monster.
             */
            if (data.encounter) {

                /*
                 * Dừng di chuyển.
                 */
                pressedKeys.clear();

                pressedButtons.clear();


                if (player) {

                    player.classList.remove(
                        "walking"
                    );
                }


                /*
                 * Chuyển sang flow encounter
                 * hiện tại của AETHERLIA.
                 */
                window.location.href =
                    "/explore";
            }

        })


        .catch(function(error) {

            console.error(
                "Lỗi kiểm tra encounter:",
                error
            );

        })


        .finally(function() {

            encounterChecking =
                false;
        });
}


/* =========================================================
   GET DIRECTION
========================================================= */

function getDirection() {

    if (
        pressedKeys.has("w") ||
        pressedKeys.has("arrowup") ||
        pressedButtons.has("UP")
    ) {

        return "UP";
    }


    if (
        pressedKeys.has("s") ||
        pressedKeys.has("arrowdown") ||
        pressedButtons.has("DOWN")
    ) {

        return "DOWN";
    }


    if (
        pressedKeys.has("a") ||
        pressedKeys.has("arrowleft") ||
        pressedButtons.has("LEFT")
    ) {

        return "LEFT";
    }


    if (
        pressedKeys.has("d") ||
        pressedKeys.has("arrowright") ||
        pressedButtons.has("RIGHT")
    ) {

        return "RIGHT";
    }


    return null;
}


/* =========================================================
   FACING
========================================================= */

function updateFacing(
    direction
) {

    if (
        !player ||
        !direction
    ) {

        return;
    }


    player.classList.remove(
        "facing-up",
        "facing-down",
        "facing-left",
        "facing-right"
    );


    player.classList.add(
        "facing-" +
        direction.toLowerCase()
    );
}


/* =========================================================
   UPDATE POSITION
========================================================= */

function updatePlayerPosition(
    x,
    y
) {

    if (!player) {
        return;
    }


    if (
        typeof x !== "number" ||
        typeof y !== "number"
    ) {

        return;
    }


    player.style.left =
        (x * 100 / 900) +
        "%";


    player.style.top =
        (y * 100 / 500) +
        "%";


    if (playerX) {

        playerX.textContent =
            x;
    }


    if (playerY) {

        playerY.textContent =
            y;
    }
}


/* =========================================================
   COLLISION EFFECT
========================================================= */

function showCollision() {

    if (!player) {
        return;
    }


    player.classList.remove(
        "collision-shake"
    );


    void player.offsetWidth;


    player.classList.add(
        "collision-shake"
    );


    setTimeout(
        function() {

            player.classList.remove(
                "collision-shake"
            );

        },
        200
    );
}


/* =========================================================
   MOVE PLAYER
========================================================= */

function movePlayer(
    direction
) {

    if (!player) {
        return;
    }


    if (!direction) {
        return;
    }


    if (movementRequestRunning) {
        return;
    }


    if (
        !csrfToken ||
        !csrfHeader
    ) {

        console.error(
            "Không tìm thấy CSRF token."
        );

        return;
    }


    movementRequestRunning =
        true;


    player.classList.add(
        "walking"
    );


    updateFacing(
        direction
    );


    const body =
        new URLSearchParams();


    body.append(
        "direction",
        direction
    );


    fetch(
        "/player/move",
        {
            method: "POST",

            headers: {

                [csrfHeader]:
                    csrfToken,

                "Content-Type":
                    "application/x-www-form-urlencoded"
            },

            body: body
        }
    )


        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Movement failed. HTTP " +
                    response.status
                );
            }


            return response.json();
        })


        .then(function(data) {

            console.log(
                "Movement:",
                data
            );


            updatePlayerPosition(
                data.x,
                data.y
            );


            if (data.facing) {

                updateFacing(
                    data.facing
                );
            }


            if (data.locationCode) {

                updateLocation(
                    data.locationCode
                );
            }


            /*
             * Chỉ check encounter
             * khi bước di chuyển thành công.
             */
            if (
                !data.blocked &&
                data.locationCode ===
                "route_grass"
            ) {

                checkRandomEncounter();
            }


            if (data.blocked) {

                showCollision();
            }

        })


        .catch(function(error) {

            console.error(
                "Lỗi di chuyển:",
                error
            );

        })


        .finally(function() {

            movementRequestRunning =
                false;
        });
}


/* =========================================================
   KEY DOWN
========================================================= */

document.addEventListener(
    "keydown",
    function(event) {

        const key =
            event.key.toLowerCase();


        if (
            key === "w" ||
            key === "a" ||
            key === "s" ||
            key === "d" ||
            key === "arrowup" ||
            key === "arrowdown" ||
            key === "arrowleft" ||
            key === "arrowright"
        ) {

            event.preventDefault();

            pressedKeys.add(key);
        }


        if (event.key === "F3") {

            event.preventDefault();

            document.body.classList.toggle(
                "debug-collision"
            );
        }

    }
);


/* =========================================================
   KEY UP
========================================================= */

document.addEventListener(
    "keyup",
    function(event) {

        const key =
            event.key.toLowerCase();


        pressedKeys.delete(key);
    }
);


/* =========================================================
   CONTROL BUTTONS
========================================================= */

const controlButtons =
    document.querySelectorAll(
        "[data-direction]"
    );


controlButtons.forEach(
    function(button) {

        const direction =
            button.dataset.direction;


        button.addEventListener(
            "pointerdown",
            function(event) {

                event.preventDefault();

                pressedButtons.add(
                    direction
                );
            }
        );


        button.addEventListener(
            "pointerup",
            function(event) {

                event.preventDefault();

                pressedButtons.delete(
                    direction
                );
            }
        );


        button.addEventListener(
            "pointerleave",
            function() {

                pressedButtons.delete(
                    direction
                );
            }
        );


        button.addEventListener(
            "pointercancel",
            function() {

                pressedButtons.delete(
                    direction
                );
            }
        );

    }
);


/* =========================================================
   MOVEMENT LOOP
========================================================= */

setInterval(
    function() {

        const direction =
            getDirection();


        if (direction) {

            movePlayer(
                direction
            );

        } else {

            if (player) {

                player.classList.remove(
                    "walking"
                );
            }
        }

    },
    140
);


/* =========================================================
   WINDOW BLUR
========================================================= */

window.addEventListener(
    "blur",
    function() {

        pressedKeys.clear();

        pressedButtons.clear();


        if (player) {

            player.classList.remove(
                "walking"
            );
        }
    }
);


/* =========================================================
   INITIAL LOCATION
========================================================= */

if (locationCodeElement) {

    updateLocation(
        locationCodeElement.textContent.trim()
    );
}