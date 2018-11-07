'use strict';

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Bubbles = function () {
    function Bubbles(_settings) {
        _classCallCheck(this, Bubbles);

        this.bRuning = false;
        this.canvas = document.getElementById('canvas');
        this.ctx = this.canvas.getContext('2d');
        this.canvas.height = window.innerHeight;
        this.canvas.width = window.innerWidth;
        this.canvasbg = document.getElementById('canvasbg');
        this.ctxbg = this.canvasbg.getContext('2d');
        this.canvasbg.height = window.innerHeight;
        this.canvasbg.width = window.innerWidth;
        this.aBubbles = [];
        this.aBgBubbles = [];
    }

    Bubbles.prototype.addBubble = function addBubble() {
        this.aBubbles.push(new Bubble());
    };

    Bubbles.prototype.addBgBubble = function addBgBubble() {
        this.aBgBubbles.push(new Bubble('rgba(192, 57, 43,0.5)', 3.5)); // Color for the bubbles in the back
    };

    Bubbles.prototype.update = function update() {

        for (var i = this.aBubbles.length - 1; i >= 0; i--) {

            this.aBubbles[i].update();

            if (!this.aBubbles[i].life) this.aBubbles.splice(i, 1);
        }

        for (var i = this.aBgBubbles.length - 1; i >= 0; i--) {

            this.aBgBubbles[i].update();

            if (!this.aBgBubbles[i].life) this.aBgBubbles.splice(i, 1);
        }

        if (this.aBubbles.length < window.innerWidth / 4) this.addBubble();

        if (this.aBgBubbles.length < window.innerWidth / 12) this.addBgBubble();
    };

    Bubbles.prototype.draw = function draw() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctxbg.clearRect(0, 0, this.canvas.width, this.canvas.height);

        for (var i = this.aBgBubbles.length - 1; i >= 0; i--) {
            this.aBgBubbles[i].draw(this.ctxbg);
        }

        for (var i = this.aBubbles.length - 1; i >= 0; i--) {
            this.aBubbles[i].draw(this.ctx);
        }
    };

    Bubbles.prototype.run = function run() {

        this.update();
        this.draw();

        if (this.bRuning) requestAnimationFrame(this.run.bind(this));
    };

    Bubbles.prototype.start = function start() {
        this.bRuning = true;
        this.run();
    };

    Bubbles.prototype.stop = function stop() {
        this.bRuning = false;
    };

    return Bubbles;
}();

var Bubble = function () {
    function Bubble() {
        var _c = arguments.length <= 0 || arguments[0] === undefined ? 'rgba(241, 196, 15,0.7)' : arguments[0]; // Color for the bubbles on the front

        var _y = arguments.length <= 1 || arguments[1] === undefined ? 0 : arguments[1];

        _classCallCheck(this, Bubble);

        this.r = rand(20, 120);
        this.life = true;
        this.x = rand(-this.r, window.innerWidth);
        this.y = rand(window.innerHeight + this.r, window.innerHeight + this.r + 20);
        this.vy = rand(.1, .5) + _y;
        this.vr = 0;
        this.vx = rand(-3, 3);
        this.c = _c;
    }

    Bubble.prototype.update = function update() {

        this.vy += .07;
        this.vr += .012;
        this.y -= this.vy;
        this.x += this.vx;

        if (this.r > 1) this.r -= this.vr;

        if (this.r <= 1) this.life = false;
    };

    Bubble.prototype.draw = function draw(ctx) {

        ctx.beginPath();
        ctx.arc(this.x, this.y, this.r, 0, 2 * Math.PI);
        ctx.fillStyle = this.c;
        ctx.fill();
    };

    return Bubble;
}();

var rand = function rand(min, max) {
    return Math.random() * (max - min) + min;
};
var onresize = function onresize() {
    oBubbles.canvasbg.width = window.innerWidth;oBubbles.canvasbg.height = window.innerHeight;oBubbles.canvas.width = window.innerWidth;oBubbles.canvas.height = window.innerHeight;
};
var oBubbles = undefined;
var init = function init() {
    oBubbles = new Bubbles();
    oBubbles.start();
};
window.onresize = onresize;
window.onload = init;