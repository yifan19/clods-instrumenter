OP_LIST = [
#Unconditional Jumps:
'goto', # : Jump to another instruction.
'goto_w', #: Wide version of goto.
#Conditional Branches:
#Integer comparisons:
'ifeq', #: Jump if value is 0.
'ifne', #: Jump if value is not 0.
'iflt', #: Jump if value is less than 0.
'ifge', #: Jump if value is greater than or equal to 0.
'ifgt', #: Jump if value is greater than 0.
'ifle', #: Jump if value is less than or equal to 0.
# Null checks:
'ifnull', #: Jump if reference is null.
'ifnonnull',#: Jump if reference is not null.
#Comparison of two values:
'if_icmpeq', #: Jump if two integers are equal.
'if_icmpne',#: Jump if two integers are not equal.
'if_icmplt',#: Jump if one integer is less than another.
'if_icmpge',#: Jump if one integer is greater than or equal to another.
'if_icmpgt',#: Jump if one integer is greater than another.
'if_icmple',#: Jump if one integer is less than or equal to another.
'if_acmpeq',#: Jump if two references are equal.
'if_acmpne',#: Jump if two references are not equal.
#Switch Instructions:

'tableswitch', #: Jump based on a table of jump offsets.
'lookupswitch', #: Jump based on a key-value map of offsets.

'ireturn', #: Return an integer.
'lreturn', #: Return a long.
'freturn', #: Return a float.
'dreturn', #: Return a double.
'areturn', #: Return an object or array reference.
# Void Return:

'return',
]

TEMPLATE = \
"""
ID={id}
variableName=foo
className={class_name}
methodName={method_name}
lineNumber=1111
parameterTypes={params}
byteCodeIndex={bci}
strategy=conditional

"""


