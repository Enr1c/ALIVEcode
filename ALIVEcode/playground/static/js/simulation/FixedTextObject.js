class FixedTextObject extends Shape {
    constructor(s, text, size, x, y, color = "black") {
        //x /= (s.canvasCamera.scale / 1000)
        //y /= (s.canvasCamera.scale / 1000)
        let scalar = 0.8
        let oldTextSize = s.textSize()
        s.textSize(size)
        let w = s.textWidth(text)
        let ascent = s.textAscent() * scalar
        let descent = s.textDescent() * scalar
        let h = ascent + descent
        super(s, [(x - w / 2), (y + h / 2)], [(x + w / 2), (y + h / 2)], [(x + w / 2), (y - h / 2)], [(x - w / 2), (y - h / 2)])
        if (!this.isRect()) throw new ShapeException('Can only create a text component of a rectangular shape')
        this.class = "FixedTextObject"
        this.text = text
        this.size = size
        this.scalar = scalar
        this.w = w
        this.h = h
        this.ascent = ascent
        this.descent = descent
        this.color = color
        this.strokeColor = this.s.color(0,0)
        s.textSize(oldTextSize)
    }

    draw() {
        if (!this.hovering && !this.alwaysStroke) this.s.noStroke()
        else this.s.stroke(this.strokeColor)

        let translateX = this.pos.x
        let translateY = -this.pos.y
        if (this.img != null) {
            this.s.translate(translateX, translateY)
            this.s.rotate(this.rotation.x)
            this.s.image(this.img, 0, 0, dist(this.vertices[0], this.vertices[1]), dist(this.vertices[1], this.vertices[2]))
            this.s.rotate(-this.rotation.x)
            this.s.translate(-translateX, -translateY)
        } else if (this.backgroundColor != null) {
            this.s.fill(this.backgroundColor)
            this.s.beginShape()
            for (let vertex of this.vertices) {
                this.s.vertex(vertex.x, -vertex.y)
            }
            this.s.endShape(this.s.CLOSE)
        }
        this.s.fill(this.color)
        this.s.translate(translateX, translateY)
        this.s.rotate(this.rotation.x)
        let oldTextSize = this.s.textSize()
        this.s.textSize(this.size)
        this.s.text(this.text, 0, 0)
        this.s.textSize(oldTextSize)
        this.s.rotate(-this.rotation.x)
        this.s.translate(-translateX, -translateY)
    }

    scale(scaleFactor, point = null) {
        this.size *= scaleFactor
        if (point == null) point = this.pos
        for (let vertex of this.vertices) {
            vertex.x = (vertex.x - point.x) * scaleFactor
            vertex.y = (vertex.y - point.y) * scaleFactor
            vertex.x += point.x
            vertex.y += point.y
        }
        if (point != this.pos) {
            this.pos.x = (this.pos.x - point.x) * scaleFactor
            this.pos.y = (this.pos.y - point.y) * scaleFactor
            this.pos.x += point.x
            this.pos.y += point.y
        }
        for (let child of this.children) {
            child.scale(scaleFactor, point)
        }
    }

    setBackgroundColor(color) {
        this.backgroundColor = color
    }
}