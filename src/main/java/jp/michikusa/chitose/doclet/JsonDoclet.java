package jp.michikusa.chitose.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;

/**
 * Doclet implementation for javadoc command.
 * This output is based on JavaScript Object Notation.
 * @author E.Sekito
 * @since 2014/07/31
 */
public class JsonDoclet {
    public static final class Option {
        public File outputFilename;

        public boolean appendMode;

        public boolean pretty;
    }

    public static boolean start( RootDoc root) {
        final Option option = parseOption( root.options());

        try (final FileOutputStream ostream = new FileOutputStream( option.outputFilename, option.appendMode);
                final JsonGenerator generator = new JsonFactory().createGenerator( ostream)) {
            if ( option.pretty) {
                generator.setPrettyPrinter( new DefaultPrettyPrinter());
            }

            write( generator, root);

            generator.flush();
        }
        catch ( IOException e) {
            throw new RuntimeException( e);
        }

        return true;
    }

    public static int optionLength( String option) {
        switch ( option) {
            case "-ofile":
                return 2;
            case "-append":
                return 1;
            case "-pretty":
                return 1;
        }
        return 0;
    }

    static Option parseOption( String[][] options) {
        final Option opt = new Option();

        for ( final String[] pair : options) {
            switch ( pair[0]) {
                case "-ofile":
                    opt.outputFilename = new File( pair[1]);
                    break;
                case "-append":
                    opt.appendMode = true;
                    break;
                case "-pretty":
                    opt.pretty = true;
                    break;
            }
        }

        if ( opt.outputFilename == null) {
            throw new IllegalArgumentException( "missing `-ofile' argument.");
        }

        return opt;
    }

    static void write( JsonGenerator g, RootDoc doc) throws IOException {
        g.writeStartObject();

        {
            g.writeArrayFieldStart( "classes");

            for ( final ClassDoc classDoc : doc.classes()) {
                writeClass( g, classDoc);
            }

            g.writeEndArray();
        }

        g.writeEndObject();
    }

    static void writeClass( JsonGenerator g, ClassDoc doc) throws IOException {
        g.writeStartObject();

        g.writeObjectField( "name", doc.qualifiedName());
        {
            g.writeArrayFieldStart( "interfaces");

            for ( final ClassDoc interfaceDoc : doc.interfaces()) {
                g.writeString( interfaceDoc.qualifiedTypeName());
            }

            g.writeEndArray();
        }
        g.writeObjectField( "superclass", (doc.superclassType() != null) ? doc.superclassType().qualifiedTypeName() : "");
        g.writeObjectField( "comment_text", doc.commentText());
        {
            final Tag tag = get( doc.tags( "since"), 0);

            g.writeObjectField( "since", (tag != null) ? tag.text() : "");
        }
        {
            g.writeArrayFieldStart( "see");

            for ( final SeeTag tag : doc.seeTags()) {
                g.writeString( tag.referencedClassName());
            }

            g.writeEndArray();
        }
        {
            g.writeArrayFieldStart( "constructors");

            for ( final ConstructorDoc ctorDoc : doc.constructors()) {
                writeConstructor( g, ctorDoc);
            }

            g.writeEndArray();
        }
        {
            g.writeArrayFieldStart( "fields");

            for ( final FieldDoc fieldDoc : doc.fields()) {
                writeField( g, fieldDoc);
            }

            g.writeEndArray();
        }
        {
            g.writeArrayFieldStart( "methods");

            for ( final MethodDoc methodDoc : doc.methods()) {
                writeMethod( g, methodDoc);
            }

            g.writeEndArray();
        }

        g.writeEndObject();
    }

    static void writeConstructor( JsonGenerator g, ConstructorDoc doc) throws IOException {
        g.writeStartObject();

        g.writeObjectField( "name", doc.name());
        g.writeObjectField( "comment_text", doc.commentText());
        {
            g.writeArrayFieldStart( "parameters");

            for ( int i = 0; i < doc.parameters().length; ++i) {
                writeMethodParameter( g, doc.parameters()[i], doc.paramTags());
            }

            g.writeEndArray();
        }
        {
            g.writeArrayFieldStart( "throws");

            for ( int i = 0; i < doc.thrownExceptionTypes().length; ++i) {
                writeThrow( g, doc.thrownExceptionTypes()[i], doc.throwsTags());
            }

            g.writeEndArray();
        }

        g.writeEndObject();
    }

    static void writeThrow( JsonGenerator g, Type type, ThrowsTag[] tags) throws IOException {
        final ThrowsTag tag = find( tags, type);

        g.writeStartObject();

        g.writeObjectField( "name", type.qualifiedTypeName());
        g.writeObjectField( "comment_text", (tag != null) ? tag.exceptionComment() : "");

        g.writeEndObject();
    }

    static void writeMethodParameter( JsonGenerator g, Parameter parameter, ParamTag[] tags) throws IOException {
        final ParamTag tag = find( tags, parameter);

        g.writeStartObject();

        g.writeObjectField( "name", parameter.name());
        g.writeObjectField( "comment_text", (tag != null) ? tag.parameterComment() : "");
        g.writeObjectField( "type", parameter.type().qualifiedTypeName());

        g.writeEndObject();
    }

    static void writeField( JsonGenerator g, FieldDoc doc) throws IOException {
        g.writeStartObject();

        g.writeObjectField( "name", doc.name());
        g.writeObjectField( "comment_text", doc.commentText());
        g.writeObjectField( "type", doc.type().qualifiedTypeName());

        g.writeEndObject();
    }

    static void writeMethod( JsonGenerator g, MethodDoc doc) throws IOException {
        g.writeStartObject();

        g.writeObjectField( "name", doc.name());
        g.writeObjectField( "comment_text", doc.commentText());
        g.writeObjectField( "return_type", doc.returnType().qualifiedTypeName());
        {
            g.writeArrayFieldStart( "parameters");

            for ( int i = 0; i < doc.parameters().length; ++i) {
                writeMethodParameter( g, doc.parameters()[i], doc.paramTags());
            }

            g.writeEndArray();
        }
        {
            g.writeArrayFieldStart( "throws");

            for ( int i = 0; i < doc.thrownExceptions().length; ++i) {
                writeThrow( g, doc.thrownExceptionTypes()[i], doc.throwsTags());
            }

            g.writeEndArray();
        }

        g.writeEndObject();
    }

    static <T> T get( T[] elements, int i) {
        if ( i < 0) {
            throw new IllegalArgumentException();
        }

        if ( i < elements.length) {
            return elements[i];
        }
        else {
            return null;
        }
    }

    static ThrowsTag find( ThrowsTag[] tags, Type type) {
        for ( final ThrowsTag tag : tags) {
            if ( type.equals( tag.exceptionType())) {
                return tag;
            }
        }
        return null;
    }

    static ParamTag find( ParamTag[] tags, Parameter param) {
        for ( final ParamTag tag : tags) {
            if ( param.name().equals( tag.parameterName())) {
                return tag;
            }
        }
        return null;
    }
}
