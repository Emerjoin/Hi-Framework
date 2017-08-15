package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public class Template extends AbstractTransformable {

    private String name;

    public Template(String html,String name){
        super(html);
        this.name = name;

    }

    public String getName() {

        return name;

    }
}
